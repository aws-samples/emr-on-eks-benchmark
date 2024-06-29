// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amazonaws.eks.tpcds

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.col
import org.apache.log4j.{Level, LogManager}
import scala.util.Try

object CreateIcebergTables {
  def main(args: Array[String]) {
    val tpcdsDataDir = args(0)
    val dsdgenDir = args(1)
    val format = Try(args(2).toString).getOrElse("parquet")
    val scaleFactor = Try(args(3).toString).getOrElse("1")
    val onlyWarn = Try(args(4).toBoolean).getOrElse(false)
    val databaseName = Try(args(5).toString).getOrElse("tpcds_db_iceberg")
    val useStringForCharAndVarchar = Try(args(6).toBoolean).getOrElse(false)
    val inferSchema = Try(args(7).toBoolean).getOrElse(false)

    println(s"DATA DIR is $tpcdsDataDir")
    val tempDatabaseName = databaseName + "_temp"

    val spark = SparkSession
      .builder
      .appName(s"TPCDS SQL Benchmark $scaleFactor GB")
      .getOrCreate()

    if (onlyWarn) {
      println(s"Only WARN")
      LogManager.getLogger("org").setLevel(Level.WARN)
    }

    val tables = new TPCDSTables(spark.sqlContext,
      dsdgenDir = dsdgenDir,
      scaleFactor = scaleFactor,
      useDoubleForDecimal = false,
      useStringForDate = false,
      useStringForCharAndVarchar = useStringForCharAndVarchar)

    Try {
      spark.sql(s"create database $tempDatabaseName")
    }
    tables.createExternalTables(tpcdsDataDir, format, tempDatabaseName,
      overwrite = true, discoverPartitions = true, inferSchema = inferSchema)

    spark.sql(s"use $tempDatabaseName")
    spark.sql(s"show tables").show(200, false)

    // Create Iceberg tables
    try {
      // Create a new database for converted iceberg tables
      spark.sql("CREATE DATABASE hadoop_catalog." + databaseName)
      val tables = spark.sql("show tables").collect()

      tables.foreach(row => {
        val source_table = "source_table => 'spark_catalog." + tempDatabaseName + "." + row.getString(1) + "', "
        val table = "table => 'hadoop_catalog." + databaseName + "." + row.getString(1) + "'"
        val command = "CALL hadoop_catalog.system.snapshot(" +
          source_table +
          table +
          ")"
        spark.sql(command)
      })
      spark.sql(s"show tables from hadoop_catalog.$databaseName").show(200, false)
      spark.stop()
      sys.exit(0)
    } catch {
      case e: Throwable => {
        println("Exception caught: " + e);
        e.printStackTrace();
        spark.stop()
        System.exit(-1);
      }
    }
  }
}