name := "AkkaInfinispanCrud"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.19"
val akkaHttpVersion = "10.1.5"
val sprayJsonVersion = "1.3.5"
val log4j2Version = "2.11.1"
val catsCoreVersion = "1.5.0"
val catsEffectVersion = "1.1.0"
val inifinispanVersion = "9.1.7.Final"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "io.spray" %% "spray-json" % sprayJsonVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4j2Version,
  "org.apache.logging.log4j" % "log4j-core" % log4j2Version,
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.infinispan" % "infinispan-embedded" % inifinispanVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)