enablePlugins(FlywayPlugin)

name := "AkkaQuillSQLCrud"

version := "0.1"

scalaVersion := "2.12.8"

val postgresqlDriverVersion = "42.2.5"
val circeVersion = "0.10.0"
val catsVersion = "1.5.0"
val catsEffectVersion = "1.1.0"
val log4j2Version = "2.11.1"
val akkaVersion = "2.5.19"
val akkaHtpVersion = "10.1.5"
val quillVersion = "2.6.0"
val scalatestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % postgresqlDriverVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHtpVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4j2Version,
  "org.apache.logging.log4j" % "log4j-core" % log4j2Version,
  "io.getquill" %% "quill-jdbc" % quillVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test
)

flywayLocations += "db/migration"
flywayUrl := "jdbc:postgresql://192.168.99.100:5432/postgres"
flywaySchemas := Seq("quill_crud")
flywayTable := "flyway_schema_history"
flywayUser := "postgres"
flywayPassword := ""
flywayPlaceholders := Map("schema" -> "quill_crud")
flywayUrl in Test := "jdbc:postgresql://192.168.99.100:5432/postgres"
flywayPlaceholders in Test := Map("schema" -> "quill_crud_test")
flywayUser in Test := "postgres"
flywaySchemas in Test := Seq("quill_crud_test")
flywayTable in Test := "flyway_schema_history_test"
flywayPassword in Test := ""