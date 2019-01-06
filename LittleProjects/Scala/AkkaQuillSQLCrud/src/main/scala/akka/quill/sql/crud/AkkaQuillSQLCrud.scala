package akka.quill.sql.crud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.quill.sql.crud.actors.ExecutorActor
import akka.quill.sql.crud.web.{RestApi, RestApiRoute}
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import org.apache.logging.log4j.LogManager

object AkkaQuillSQLCrud extends App {
  val logger = LogManager.getLogger("AkkaQuillSQLCrud")

  logger.info("Starting AkkaQuillSQLCrud")
  implicit val system = ActorSystem("akkaQuillSQLCrud")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val router = system.actorOf(RoundRobinPool(10).props(ExecutorActor.props()), "executors")
  val api = RestApi(router)
  val apiRoute = RestApiRoute(api)

  Http().bindAndHandle(apiRoute.apiRoute, "localhost", 8080)
}
