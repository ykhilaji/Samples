name := "CalculatorCQRS"

version := "0.1"

scalaVersion := "2.12.10"

scalacOptions := Seq(
  "-encoding", "utf8",
//  "-Xfatal-warnings", // - disable deprecation warnings
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

enablePlugins(Antlr4Plugin, JavaAppPackaging)

// Antlr4 settings
antlr4Version in Antlr4 := "4.7.2"
antlr4PackageName in Antlr4 := Some("com.nryanov.calculator")
antlr4GenListener in Antlr4 := false
antlr4GenVisitor in Antlr4 := true
antlr4TreatWarningsAsErrors in Antlr4 := true

// Dependencies
libraryDependencies ++= Seq(
  // cats
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.pure4s" %% "logger4s-cats" % "0.3.1",
  // pureconfig
  "com.github.pureconfig" %% "pureconfig" % "0.12.1",
  // doobie
  "org.tpolecat" %% "doobie-core" % "0.8.6",
  "org.tpolecat" %% "doobie-postgres" % "0.8.6",
  "org.tpolecat" %% "doobie-hikari" % "0.8.6",
  // flyway
  "org.flywaydb" % "flyway-core" % "6.1.3",
  // fs2
  "co.fs2" %% "fs2-core" % "2.1.0",
  "dev.profunktor" %% "fs2-rabbit" % "2.1.0",
  // akka
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.akka" %% "akka-stream" % "2.6.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.1",
  "de.heikoseeberger" %% "akka-http-circe" % "1.30.0",
  // tapir
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.12.12",
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.12.12",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.12.12",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http" % "0.12.12",
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "0.12.12",
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe" % "0.12.12",
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "0.12.12",
  // prometheus
  "io.prometheus" % "simpleclient" % "0.8.0",
  "io.prometheus" % "simpleclient_common" % "0.8.0",
  "io.prometheus" % "simpleclient_hotspot" % "0.8.0"
)

mainClass in Compile := Some("com.nryanov.calculator.CalculatorApp")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

javaSource in Antlr4 := (sourceManaged in Compile).value