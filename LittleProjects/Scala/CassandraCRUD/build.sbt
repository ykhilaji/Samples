name := "CassandraCRUD"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "io.spray" %% "spray-json" % "1.3.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.5.0",
  "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.5.0",
  "org.slf4j" % "slf4j-simple" % "1.7.+", // remove while testing
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.cassandraunit" % "cassandra-unit" % "3.3.0.2" % Test,
  "org.mockito" % "mockito-all" % "1.9.5" % Test
)
