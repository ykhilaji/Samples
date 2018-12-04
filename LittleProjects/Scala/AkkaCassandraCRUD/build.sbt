name := "AkkaCassandraCRUD"

version := "0.1"

scalaVersion := "2.12.7"

val akkaVersion = "2.5.18"
val akkaHttpVersion = "10.1.5"
val sprayJsonVersion = "1.3.5"
val phantomCassandraVersion = "2.29.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.spray" %% "spray-json" % sprayJsonVersion,
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.scala-lang" % "scala-reflect" % "2.12.7",
  "com.outworkers" %% "phantom-dsl" % phantomCassandraVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)
