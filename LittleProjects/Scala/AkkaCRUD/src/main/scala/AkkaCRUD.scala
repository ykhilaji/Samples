import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import model.{Entity, Response}
import model.JsonWrapper._
import spray.json._
import org.slf4j.LoggerFactory
import service.EntityService

import scala.io.StdIn

object AkkaCRUD extends App {
  val logger = LoggerFactory.getLogger("AkkaCRUD")
  implicit val actorSystem = ActorSystem("AkkaCRUD")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val entityService = EntityService()

  val route =
    path("") {
      get {
        parameters('id.as[Long].?) {
          id => {
            id.getOrElse(0) match {
              case 0 => complete(HttpEntity(ContentTypes.`application/json`, entityService.selectAll().toJson.toString))
              case x: Long if x > 0 => complete(HttpEntity(ContentTypes.`application/json`, entityService.select(x).toJson.toString))
              case _ => complete(HttpEntity(ContentTypes.`application/json`, Response("ERROR", "ID should be > 0").toJson.toString))
            }
          }
        }
      } ~ put {
        entity(as[Entity]) {
          entity => {
            logger.info(entity.toString)
            complete(HttpEntity(ContentTypes.`application/json`, entityService.update(entity).toJson.toString))
          }
        }
      } ~ post {
        entity(as[Entity]) {
          entity => {
            logger.info(entity.toString)
            complete(HttpEntity(ContentTypes.`application/json`, entityService.insert(entity).toJson.toString))
          }
        }
      } ~ delete {
        parameters('id.as[Long]) {
          id => {
            complete(HttpEntity(ContentTypes.`application/json`, entityService.delete(id).toJson.toString))
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  logger.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())
}
