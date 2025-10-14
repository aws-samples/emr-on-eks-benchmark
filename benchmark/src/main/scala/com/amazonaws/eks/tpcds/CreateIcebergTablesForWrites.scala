// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.eks.tpcds

import com.amazonaws.eks.tpcds.TpcdsDdlStatements._
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.SparkSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import scala.sys.process._
import scala.util.Try

object CreateIcebergTablesForWrites {
  def main(args: Array[String]): Unit = {
    val lstBenchDataDir = args(0)
    val deltaMergeSourcesDir = args(1)
    val s3PathPrefixDestinationDir = args(2)
    val format = Try(args(3)).getOrElse("parquet")
    val scaleFactor = Try(args(4)).getOrElse("1")
    val onlyWarn = Try(args(5).toBoolean).getOrElse(false)
    val databaseName = Try(args(6)).getOrElse("tpcds_writes_db_iceberg")

    println(s"LST BENCH DATA DIR is $lstBenchDataDir")
    println(s"DELTA MERGE SOURCES DIR is $deltaMergeSourcesDir")

    // Temp database for the hive tables
    val tempDatabaseName = databaseName + "_temp_hive"

    val spark = SparkSession
      .builder
      .appName(s"TPCDS SQL Benchmark create tables for write benchmark with scale $scaleFactor GB and $format format")
      .getOrCreate()

    if (onlyWarn) {
      println(s"Only WARN")
      LogManager.getLogger("org").setLevel(Level.WARN)
    }

    val inputs = Seq(
      "web_returns_file005_match000_notmatch005",
      "web_returns_file005_match000_notmatch010",
      "web_returns_file005_match000_notmatch050",
      "web_returns_file005_match000_notmatch100",
      "web_returns_file005_match001_notmatch010",
      "web_returns_file005_match005_notmatch000",
      "web_returns_file005_match010_notmatch000",
      "web_returns_file005_match100_notmatch001",
      "web_returns_file005_match010_notmatch010",
      "web_returns_file005_match050_notmatch001",
      "web_returns_file005_match099_notmatch001",
      "web_returns_file050_match001_notmatch0001",
      "web_returns_file100_match001_notmatch0001"
    )

    val lstSourceToCopy = Seq(
      ("catalog_returns", catalog_returns_ddl),
      ("catalog_sales", catalog_sales_ddl),
      ("inventory", inventory_ddl),
      ("store_returns", store_returns_ddl),
      ("store_sales", store_sales_ddl),
      ("web_returns", web_returns_ddl),
      ("web_sales", web_sales_ddl),
      ("date_dim", date_dim_ddl)
    )

    // Copy delta merge sources to the destination location
    val deltaCPS3Command = Seq("s3-dist-cp", "--src", deltaMergeSourcesDir, "--dest", s"$s3PathPrefixDestinationDir/$tempDatabaseName")
    val deltaCPOutput = deltaCPS3Command.!
    if (deltaCPOutput != 0) {
      println("Failed to copy over delta merge sources")
      sys.exit(-1)
    }

    // Create a temp database for hive tables
    spark.sql(s"CREATE DATABASE IF NOT EXISTS $tempDatabaseName")
    spark.sql(s"USE $tempDatabaseName")
    spark.sql("show tables").show()

    // Create hive tables for the delta merge sources
    inputs.foreach { tableName =>
      val sqlForTable = createDeltaMergeSourceSql
        .replace("tableName", tableName)
        .replace("s3PathPrefix", s3PathPrefixDestinationDir)
        .replace("s3PathTargetDatabase", tempDatabaseName)
      spark.sql(sqlForTable).show()
    }

    // Create LST merge tables
    val tasks = for ((tableName, ddl) <- lstSourceToCopy) yield Future {
      val s3CPCommand = Seq("s3-dist-cp", "--src", s"$lstBenchDataDir/$tableName", "--dest", s"$s3PathPrefixDestinationDir/$tempDatabaseName/$tableName")
      val output = s3CPCommand.!
      if (output != 0) {
        println("Failed to copy over LST data")
        sys.exit(-1)
      }
      val sqlForTable = ddl
        .replace("s3PathPrefix", s3PathPrefixDestinationDir)
        .replace("s3PathTargetDatabase", tempDatabaseName)
      spark.sql(sqlForTable).show()
      if (!tableName.contains("date_dim")) {
        spark.sql(s"MSCK REPAIR TABLE $tableName").show()
      }
      println(s"Finish: $tableName")
    }
    val allTasks = Future.sequence(tasks)
    println("Awaiting creation...")
    Await.result(allTasks, 36000.seconds)


    // Create Hive tables for merge targets
    // q0 is for warming up cluster
    val deltaMergeTargetTasks = for (index <- 0 to 16) yield Future {
      val s3Command = Seq("s3-dist-cp", "--src", s"$s3PathPrefixDestinationDir/$tempDatabaseName/web_returns/", "--dest", s"$s3PathPrefixDestinationDir/$tempDatabaseName/web_returns_q$index/")
      s3Command.!!
      val sqlForTable = createDeltaMergeTargetTableSql
        .replace("tableName", s"web_returns_q$index")
        .replace("s3PathPrefix", s3PathPrefixDestinationDir)
        .replace("s3PathTargetDatabase", tempDatabaseName)
      spark.sql(sqlForTable)
      spark.sql(s"MSCK REPAIR TABLE web_returns_q$index")
      println(s"Finish: web_returns_q$index")
      index
    }

    val allDeltaMergeTargetTasks = Future.sequence(deltaMergeTargetTasks)

    println("Awaiting creation...")
    Await.result(allDeltaMergeTargetTasks, 36000.seconds)
    println("Finished creation...")
    spark.sql("show tables").show()


    // Create Iceberg tables
    spark.sql(s"CREATE DATABASE IF NOT EXISTS hadoop_catalog.$databaseName")
    val tables = spark.sql("show tables")
    tables.collect().map(r => r(1).toString).foreach(t => {
      val source_table = "source_table => 'spark_catalog." + tempDatabaseName + "." + t + "', "
      val target_table = "table => 'hadoop_catalog." + databaseName + t + "', "
      val location = "location => '" + s3PathPrefixDestinationDir + "/" + databaseName + "/" + t + "_iceberg/" + "'"
      if (true) {
        val command = "CALL iceberg.system.snapshot(" +
          source_table +
          target_table +
          location +
          ")"
        spark.sql(command)
        Thread.sleep(64000)
      }
    })

    spark.sql("show tables from").show()

    sys.exit(0)
  }
}
