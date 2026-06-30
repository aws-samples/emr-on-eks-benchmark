// Your sbt build file. Guides on how to write one can be found at
// https://www.scala-sbt.org/1.x/docs/index.html

name := "spark-sql-perf"

organization := "com.databricks"

scalaVersion := "2.13.16"

crossScalaVersions := Seq("2.13.16")

// All Spark Packages need a license
licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

val sparkVersion = "4.0.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core"  % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql"   % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive"  % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"

libraryDependencies += "com.twitter" %% "util-jvm" % "21.2.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "org.yaml" % "snakeyaml" % "2.2"

fork := true

console / initialCommands :=
  """
    |import org.apache.spark.sql._
    |import org.apache.spark.sql.functions._
    |import org.apache.spark.sql.types._
    |import org.apache.spark.sql.hive.test.TestHive
    |import TestHive.implicits
    |import TestHive.sql
    |
    |val sqlContext = TestHive
    |import sqlContext.implicits._
  """.stripMargin

val runBenchmark = inputKey[Unit]("runs a benchmark")

runBenchmark := {
  import complete.DefaultParsers._
  val args = spaceDelimited("[args]").parsed
  val scalaRun = (Compile / run / runner).value
  val classpath = (Compile / fullClasspath).value
  scalaRun.run("com.databricks.spark.sql.perf.RunBenchmark", classpath.map(_.data), args,
    streams.value.log)
}

val runMLBenchmark = inputKey[Unit]("runs an ML benchmark")

runMLBenchmark := {
  import complete.DefaultParsers._
  val args = spaceDelimited("[args]").parsed
  val scalaRun = (Compile / run / runner).value
  val classpath = (Compile / fullClasspath).value
  scalaRun.run("com.databricks.spark.sql.perf.mllib.MLLib", classpath.map(_.data), args,
    streams.value.log)
}

// sbt-assembly merge settings so `sbt assembly` works.
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _                            => MergeStrategy.first
}
