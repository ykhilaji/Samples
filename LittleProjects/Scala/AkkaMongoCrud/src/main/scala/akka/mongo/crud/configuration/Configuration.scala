package akka.mongo.crud.configuration

import com.typesafe.config.{Config, ConfigFactory}

object Configuration {
  val crudCfg: Config = ConfigFactory.load().getConfig("crud")

  def host: String = crudCfg.getString("mongo.host")

  def port: Int = crudCfg.getInt("mongo.port")
}
