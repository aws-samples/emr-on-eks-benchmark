// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.eks.tpcds

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.SparkSession
import scala.util.{Failure, Success, Try}
import sys.process._

object CleanUpMergeTables {

  def main(args: Array[String]): Unit = {
    println("Starting CleanUpMergeTables")
    require(args.length >= 3, "Usage: CleanUpMergeTables <parquetDir> <icebergDir> <database> [onlyWarn]")

    val parquetSourceDirectory = args(0)
    val icebergWarehouseDirectory = args(1)
    val hive_database = args(2)
    val onlyWarn = args.lift(3).exists(_.toBoolean)

    val spark = SparkSession.builder
      .appName(s"TPCDS Clean up merge tables for $hive_database")
      .enableHiveSupport()
      .getOrCreate()

    if (onlyWarn) {
      println("Only WARN")
      LogManager.getLogger("org").setLevel(Level.WARN)
    }

    Try {
      spark.sql(s"DROP DATABASE IF EXISTS $hive_database CASCADE")

      deleteS3Directory(parquetSourceDirectory)
      deleteS3Directory(icebergWarehouseDirectory)
    } match {
      case Success(_) => println(s"Successfully cleaned up $hive_database")
      case Failure(e) =>
        println(s"Failed to clean up $hive_database: ${e.getMessage}")
        e.printStackTrace()
    }

    spark.stop()
  }

  private def deleteS3Directory(path: String): Unit = {
    val normalizedPath = if (path.endsWith("/")) path else s"$path/"
    println(s"Deleting S3 directory: $normalizedPath")
    val output = Seq("aws", "s3", "rm", normalizedPath, "--recursive").!!
    println(output)
  }
}
