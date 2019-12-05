name := "CatsCalculator"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions := List("-encoding", "utf8")

enablePlugins(Antlr4Plugin, JavaAppPackaging)

// Antlr4 settings
antlr4Version in Antlr4 := "4.7.2"
antlr4PackageName in Antlr4 := Some("com.github.nryanov.calculator")
antlr4GenListener in Antlr4 := false
antlr4GenVisitor in Antlr4 := true
antlr4TreatWarningsAsErrors in Antlr4 := true

// Dependencies
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

mainClass in Compile := Some("com.github.nryanov.calculator.CalculatorApp")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

javaSource in Antlr4 := (sourceManaged in Compile).value
