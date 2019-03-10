name := "SparkSamples"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"
val postgresqlVersion = "42.2.1"
val cassandraConnectorVersion = "2.0.7"
val log4jVersion = "2.11.2"
val kafkaIntegrationVersion = "2.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.postgresql" % "postgresql" % postgresqlVersion,
  "com.datastax.spark" %% "spark-cassandra-connector" % cassandraConnectorVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % kafkaIntegrationVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion
)