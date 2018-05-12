name := "SparkSamples"

version := "0.1"

scalaVersion := "2.11.12"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.1",
  "org.apache.spark" %% "spark-sql" % "2.2.1",
  "org.apache.spark" %% "spark-streaming" % "2.2.1" % "provided",
  "org.postgresql" % "postgresql" % "42.2.1",
  "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.7"
)
