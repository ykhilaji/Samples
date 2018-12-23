package akka.nifi.stream.postgres.configuration

import com.typesafe.config.{Config, ConfigFactory}

object Configuration {
  val config: Config = ConfigFactory.load()
  val database: Config = config.getConfig("app.repository.database")

  val dbUrl: String = database.getString("url")
  val dbUser: String = database.getString("user")
  val dbPass: String = database.getString("password")
  val schema: String = database.getString("schema")
}
