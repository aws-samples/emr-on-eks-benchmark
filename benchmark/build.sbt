name := "eks-spark-benchmark"

version := "1.0"

scalaVersion := "2.13.16"

javacOptions ++= Seq("-source", "17", "-target", "17")

scalacOptions += "-release:17"

unmanagedBase := baseDirectory.value / "libs"

// Dependencies required for this project
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "4.0.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "4.0.0" % "provided",
  // JSON serialization
  "org.json4s" %% "json4s-native" % "4.0.7",
  // scala logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
)

// Remove stub classes
assembly / assemblyMergeStrategy := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
  case x =>
    val old = (assembly / assemblyMergeStrategy).value
    old(x)
}

// Exclude the Scala runtime jars
assembly / assemblyOption ~= { _.withIncludeScala(false) }
