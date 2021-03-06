name := "AkkaMemcachedCrud"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.14",
  "com.typesafe.akka" %% "akka-http" % "10.1.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.4",
  "io.spray" %% "spray-json" % "1.3.4",
  "net.spy" % "spymemcached" % "2.12.1"
)
