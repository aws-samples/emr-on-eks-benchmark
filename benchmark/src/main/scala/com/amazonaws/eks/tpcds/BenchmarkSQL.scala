// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amazonaws.eks.tpcds

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.col
import org.apache.log4j.{Level, LogManager}
import scala.util.Try

object BenchmarkSQL {
  def main(args: Array[String]) {
    val resultLocation = args(0)
    val scaleFactor = Try(args(1).toString).getOrElse("1")
    val iterations = args(2).toInt
    val optimizeQueries = Try(args(3).toBoolean).getOrElse(false)
    val filterQueries = Try(args(4).toString).getOrElse("")
    val onlyWarn = Try(args(5).toBoolean).getOrElse(false)
    val databaseName = Try(args(6).toString).getOrElse("tpcds_db")
    val benchmarkType = Try(args(7).toString).getOrElse("read")

    val timeout = 24*60*60

    val spark = SparkSession
      .builder
      .appName(s"TPCDS SQL $benchmarkType Benchmark with $scaleFactor GB")
      .getOrCreate()

    if (onlyWarn) {
      println(s"Only WARN")
      LogManager.getLogger("org").setLevel(Level.WARN)
    }

    spark.sqlContext.sql(s"USE $databaseName")

    val tpcds = new TPCDS(spark.sqlContext)

    var query_filter : Seq[String] = Seq()
    if (!filterQueries.isEmpty) {
      println(s"Running only queries: $filterQueries")
      query_filter = filterQueries.split(",").toSeq
    }

    val queries = benchmarkType match {
      case "read" => tpcds.tpcds2_13Queries
      case "write" => tpcds.mergeQueries
    }

    val filtered_queries = query_filter match {
      case Seq() => queries
      case _ => queries.filter(q => query_filter.contains(q.name))
    }

    // Start experiment
    val experiment = tpcds.runExperiment(
      filtered_queries,
      iterations = iterations,
      resultLocation = resultLocation,
      forkThread = true,
      isWriteBenchmark = benchmarkType == "write")

    experiment.waitForFinish(timeout)

    // Collect general results
    val resultPath = experiment.resultPath
    println(s"Reading result at $resultPath")
    val specificResultTable = spark.read.json(resultPath)
    specificResultTable.show()

    // Summarize results
    val result = specificResultTable
      .withColumn("result", explode(col("results")))
      .withColumn("executionSeconds", col("result.executionTime")/1000)
      .withColumn("queryName", col("result.name"))
    result.select("iteration", "queryName", "executionSeconds").show()
    println(s"Final results at $resultPath")

    val aggResults = result.groupBy("queryName").agg(
      callUDF("percentile", col("executionSeconds").cast("double"), lit(0.5)).as('medianRuntimeSeconds),
      callUDF("min", col("executionSeconds").cast("double")).as('minRuntimeSeconds),
      callUDF("max", col("executionSeconds").cast("double")).as('maxRuntimeSeconds)
    ).orderBy(col("queryName"))
    aggResults.repartition(1).write.csv(s"$resultPath/summary.csv")
    aggResults.show(10)

    spark.stop()
  }
}