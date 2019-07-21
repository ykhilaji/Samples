resolvers += Resolver.bintrayRepo("alpeb", "sbt-plugins")

// scala-thrift plugin
addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "19.4.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25")
