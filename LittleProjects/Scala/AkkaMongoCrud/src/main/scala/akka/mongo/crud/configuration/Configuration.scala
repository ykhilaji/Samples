package akka.mongo.crud.configuration

import com.typesafe.config.{Config, ConfigFactory}

object Configuration {
  val crudCfg: Config = ConfigFactory.load().getConfig("crud")

  val host: String = crudCfg.getString("mongo.host")

  val port: Int = crudCfg.getInt("mongo.port")

  val database: String = crudCfg.getString("mongo.database")
}
