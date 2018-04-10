name := "AkkaCRUD"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.2.+",
  "org.postgresql" % "postgresql" % "42.2.1",
  "org.slf4j" % "slf4j-simple" % "1.7.+",
  "io.spray" %%  "spray-json" % "1.3.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
