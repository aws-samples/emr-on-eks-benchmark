/*
 * Copyright 2015 Databricks Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.databricks.spark.sql.perf.tpcds

import com.databricks.spark.sql.perf.{Benchmark, ExecutionMode}
import com.databricks.spark.sql.perf.ExecutionMode.CollectResults
import org.apache.commons.io.IOUtils

/**
* This implements the write benchmarks from LST bench and few other DMLs from Delta write benchmark
  */
trait TPCDS_Writes extends Benchmark {

  import ExecutionMode._

  val queryNamesMerge = Seq(
    "m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10",
    "m11", "m12", "m13", "m14", "m15", "m16", "m17", "m18", "m19", "m20",
    "m21", "m22", "m23", "m24", "m25", "m26", "m27", "m28", "m29", "m30",
    "m31", "m32", "m33", "m34", "m35", "m36", "m37"
  )

  val mergeQueries = queryNamesMerge.map { queryName =>
    val queryContent: String = IOUtils.toString(
      getClass().getClassLoader().getResourceAsStream(s"tpcds_writes/$queryName.sql"))
    Query(queryName, queryContent, description = "TPCDS write Query",
      executionMode = CollectResults)
  }
}
