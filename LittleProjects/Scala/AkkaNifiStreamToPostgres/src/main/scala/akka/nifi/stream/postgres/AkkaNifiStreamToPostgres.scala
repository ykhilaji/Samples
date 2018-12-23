package akka.nifi.stream.postgres

import akka.actor.ActorSystem
import akka.nifi.stream.postgres.configuration.DataSource
import akka.nifi.stream.postgres.web.HttpListener
import akka.stream.ActorMaterializer
import org.apache.logging.log4j.LogManager

object AkkaNifiStreamToPostgres extends App {
  val logger = LogManager.getLogger("root")
  logger.info("Starting application")

  implicit val system = ActorSystem("akka")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  DataSource.init()
  HttpListener().start()
}
