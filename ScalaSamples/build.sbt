name := "ScalaSamples"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc"     %% "scalikejdbc-async" % "0.8.+",
  "org.scalikejdbc" %% "scalikejdbc"        % "3.2.+",
  "org.postgresql" % "postgresql" % "42.2.1",
  "com.github.mauricio" %% "postgresql-async"  % "0.2.+",
  "org.slf4j"           %  "slf4j-simple"      % "1.7.+"
)
