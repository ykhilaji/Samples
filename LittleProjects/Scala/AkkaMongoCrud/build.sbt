name := "AkkaMongoCrud"

version := "0.1"

scalaVersion := "2.12.8"

parallelExecution in Test := false

val log4j2Version = "2.11.1"
val akkaStreamsVersion = "2.5.18"
val akkaHttpVersion = "10.1.5"
val catsCoreVersion = "1.5.0"
val catsEffectVersion = "1.1.0"
val mongoScalaDriverVersion = "2.4.2"
val scalaTestVersion = "3.0.5"
val sprayJsonVersion = "1.3.5"

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % mongoScalaDriverVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamsVersion,
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4j2Version,
  "org.apache.logging.log4j" % "log4j-core" % log4j2Version,
  "org.apache.logging.log4j" % "log4j-mongodb3" % log4j2Version,
  "io.spray" %% "spray-json" % sprayJsonVersion,
  "org.scalatest" % "scalatest_2.12" % scalaTestVersion % Test
)