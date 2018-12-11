package akka.mongo.crud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.mongo.crud.service.WebService
import akka.mongo.crud.web.HttpListener
import akka.stream.ActorMaterializer
import org.apache.logging.log4j.LogManager

object AkkaMongoCrud extends App {
  val logger = LogManager.getLogger("crud")
  logger.info("Starting akka.mongo.crud app")

  implicit val system = ActorSystem("akka-mongo-crud")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  Http().bindAndHandle(HttpListener(WebService()).api, "localhost", 8080)
  logger.info("Akka mongo crud app has started")
}
