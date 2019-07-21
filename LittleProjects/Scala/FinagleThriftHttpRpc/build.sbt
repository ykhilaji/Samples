name := "FinagleThriftHttpRpc"

version := "0.1"

scalaVersion := "2.12.8"

enablePlugins(Antlr4Plugin, JavaAppPackaging, DockerPlugin)

antlr4Version in Antlr4 := "4.7.2"
antlr4PackageName in Antlr4 := Some("finagle.rpc")
antlr4GenListener in Antlr4 := false
antlr4GenVisitor in Antlr4 := true
antlr4TreatWarningsAsErrors in Antlr4 := true

scalacOptions := List("-encoding", "utf8")

val scroogeVersion = "19.4.0"
val libthriftVersion = "0.12.0"
val finagleVersion = "19.6.0"
val log4jVersion = "2.12.0"
val configVersion = "1.3.4"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % configVersion,
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "finagle-thriftmux" % finagleVersion,
  "org.apache.thrift" % "libthrift" % libthriftVersion,
  "com.twitter" %% "scrooge-core" % scroogeVersion exclude("com.twitter", "libthrift"),
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion
)

// otherwise, folder antlr4 in src_managed/main will be add twice as source:
// firstly - from scrooge plugin (src_managed/main), secondly - from antlr4 plugin (src_managed/main/antlr4)
javaSource in Antlr4 := (sourceManaged in Compile).value

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

mainClass in Compile := Some("finagle.rpc.FinagleRpc")
packageName in Docker := "finagle-rpc"
version in Docker := "0.1"