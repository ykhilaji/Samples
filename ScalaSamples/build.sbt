name := "ScalaSamples"

version := "1.0"

scalaVersion := "2.12.1"

val akkaVersion = "2.5.17"
val scalaTestVersion = "3.0.5"
val scalikeJdbcVersion = "3.3.0"
val slickVersion = "3.2.0"
val shapeless = "2.3.3"
val doobieVersion = "0.6.0"
val quillVersion = "2.6.0"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-simple" % "1.7.+",
    "org.scalatest" % "scalatest_2.12" % scalaTestVersion % Test
  )
)

lazy val slickSamples = (project in file("slick"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion
    )
  )

lazy val akkaSamples = (project in file("akka"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
    )
  )

lazy val scalikeJdbcSamples = (project in file("scalikejdbc"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalikejdbc" %% "scalikejdbc" % scalikeJdbcVersion,
      "org.postgresql" % "postgresql" % "42.2.1",
      "org.scalikejdbc" %% "scalikejdbc-test" % scalikeJdbcVersion % Test
    )
  )

lazy val scalikeAsyncJdbcSamples = (project in file("scalikeasyncjdbc"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalikejdbc" %% "scalikejdbc-async" % "0.8.+",
      "com.github.mauricio" %% "postgresql-async" % "0.2.+",
      "org.scalikejdbc" %% "scalikejdbc-test" % scalikeJdbcVersion % Test
    )
  )

lazy val shapelessSamples = (project in file("shapeless"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % shapeless
    )
  )

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
    )
  )

lazy val doobie = (project in file("doobie"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-specs2" % doobieVersion

    )
  )

lazy val quill = (project in file("quill"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.2.5",
      "io.getquill" %% "quill-jdbc" % quillVersion,
      "io.getquill" %% "quill-cassandra" % quillVersion
    )
  )