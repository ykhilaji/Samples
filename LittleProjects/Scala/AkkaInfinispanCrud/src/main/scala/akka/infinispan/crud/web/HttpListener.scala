package akka.infinispan.crud.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.infinispan.crud.service.EntityService
import spray.json._
import JsonFormatter._
import akka.infinispan.crud.model.Entity
import org.apache.logging.log4j.{LogManager, Logger}

import scala.util.{Failure, Success, Try}

class HttpListener {
  val logger: Logger = LogManager.getLogger("rest-api")
  val service: EntityService = EntityService()

  def completeException(reason: Throwable): Route = complete(StatusCodes.BadRequest -> reason.getLocalizedMessage)

  def completeSuccess(js: JsValue): Route = complete(StatusCodes.OK -> js.toString())

  def completeOk(): Route = complete(StatusCodes.OK -> "ok")

  def completeNoContent(): Route = complete(StatusCodes.NoContent)

  def execute[A](f: => Either[Throwable, A])(pf: PartialFunction[Either[Throwable, A], Route]): Route = pf.orElse[Either[Throwable, A], Route]({
    case Left(reason) => completeException(reason)
  })(f)

  def parseEntity(e: String): Try[Entity] = Try(e.parseJson.convertTo[Entity])

  val api: Route = pathPrefix("api") {
    pathEndOrSingleSlash {
      get {
        parameter('id.as[Long].?) {
          case Some(i) => execute(service.findOne(i))({
            case Right(e) => e match {
              case Some(entity) => completeSuccess(entity.toJson)
              case None => completeNoContent()
            }
          })
          case None => execute(service.findAll())({
            case Right(e) => completeSuccess(e.toJson)
          })
        }
      } ~ delete {
        parameter('id.as[Long].?) {
          case Some(i) => execute(service.delete(i))({
            case Right(_) => completeOk()
          })
          case None => execute(service.deleteAll())({
            case Right(_) => completeOk()
          })
        }
      }
    } ~ requestEntityEmpty {
      complete(StatusCodes.BadRequest -> "empty body")
    } ~ requestEntityPresent {
      entity(as[String]) {
        body => parseEntity(body) match {
          case Success(value) =>
            post {
              execute(service.update(value))({
                case Right(_) => completeOk()
              })
            } ~ put {
              execute(service.save(value))({
                case Right(_) => completeOk()
              })
            }
          case Failure(exception) => completeException(exception)
        }
      }
    }
  }
}

object HttpListener {
  def apply(): HttpListener = new HttpListener()
}