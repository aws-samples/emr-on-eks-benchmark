// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amazonaws.eks.tpch

import com.databricks.spark.sql.perf.tpch.{TPCH, TPCHTables}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.util.Try

/**
 * TPC-H benchmark runner, the counterpart of com.amazonaws.eks.tpcds.BenchmarkSQL.
 * Tables are registered as temporary views over Parquet files (no catalog / no Glue).
 *
 * Arguments: <tpch data dir> <result location> <format> <scale factor> <iterations> [queries, e.g. "Q1,Q2" (default all)]
 */
object BenchmarkSQL {
  def main(args: Array[String]): Unit = {
    val dataDir = args(0)
    val resultLocation = args(1)
    val format = Try(args(2)).getOrElse("parquet")
    val scaleFactor = Try(args(3)).getOrElse("1")
    val iterations = args(4).toInt
    val filterQueries = Try(args(5)).getOrElse("")

    val timeout = 24 * 60 * 60
    println(s"DATA DIR is $dataDir")

    val spark = SparkSession.builder.appName(s"TPCH SQL Benchmark $scaleFactor GB").getOrCreate()

    // dbgen is only needed for data generation, not for running queries
    val tables = new TPCHTables(spark.sqlContext,
      dbgenDir = "/unused",
      scaleFactor = scaleFactor,
      useDoubleForDecimal = false,
      useStringForDate = false)
    tables.createTemporaryTables(dataDir, format)

    val tpch = new TPCH(spark.sqlContext)
    val queryFilter = filterQueries.split(",").map(_.trim).filter(_.nonEmpty).map(_.toUpperCase).toSet
    val queries = if (queryFilter.isEmpty) tpch.queries else tpch.queries.filter(q => queryFilter.contains(q.name.toUpperCase))
    println(s"Running ${queries.size} queries x $iterations iteration(s)")

    val experiment = tpch.runExperiment(queries, iterations = iterations, resultLocation = resultLocation, forkThread = true)
    experiment.waitForFinish(timeout)

    val resultPath = experiment.resultPath
    println(s"Reading result at $resultPath")
    val result = spark.read.json(resultPath)
      .withColumn("result", explode(col("results")))
      .withColumn("executionSeconds", col("result.executionTime") / 1000)
      .withColumn("queryName", col("result.name"))
    result.select("iteration", "queryName", "executionSeconds").show(200, false)

    val aggResults = result.groupBy("queryName").agg(
      callUDF("percentile", col("executionSeconds").cast("double"), lit(0.5)).as("medianRuntimeSeconds"),
      min(col("executionSeconds").cast("double")).as("minRuntimeSeconds"),
      max(col("executionSeconds").cast("double")).as("maxRuntimeSeconds")
    ).orderBy(col("queryName"))
    aggResults.repartition(1).write.csv(s"$resultPath/summary.csv")
    aggResults.show(30, false)
    spark.stop()
  }
}
