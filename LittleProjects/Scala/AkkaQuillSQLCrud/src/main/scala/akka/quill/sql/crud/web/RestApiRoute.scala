package akka.quill.sql.crud.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.quill.sql.crud.model.Entity
import Formatter._
import io.circe.parser._
import io.circe.syntax._

import scala.util.{Failure, Success}

class RestApiRoute(api: Api[Entity, Long]) {
  val apiRoute: Route = pathPrefix("api") {
    get {
      pathEnd {
        parameter('id.as[Long].?) {
          case Some(id) => onComplete(api.findOne(id)) {
            case Success(opt) => opt match {
              case Some(e) => complete(StatusCodes.OK -> e.asJson.toString)
              case None => complete(StatusCodes.BadRequest -> s"Object with id: $id does not exist")
            }
            case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
          }
          case None => onComplete(api.findAll()) {
            case Success(v) => complete(StatusCodes.OK -> v.asJson.toString)
            case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
          }
        }
      }
    } ~ put {
      pathEndOrSingleSlash {
        requestEntityEmpty {
          complete(StatusCodes.BadRequest -> "Empty body")
        } ~ requestEntityPresent {
          entity(as[String]) {
            body => {
              parse(body).map(_.as[Entity]) match {
                case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                case Right(r) => r match {
                  case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                  case Right(v) => onComplete(api.save(v)) {
                    case Success(e) => complete(StatusCodes.BadRequest -> e.asJson.toString)
                    case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                  }
                }
              }
            }
          }
        }
      }
    } ~ post {
      pathEndOrSingleSlash {
        requestEntityEmpty {
          complete(StatusCodes.BadRequest -> "Empty body")
        } ~ requestEntityPresent {
          entity(as[String]) {
            body => {
              parse(body).map(_.as[Entity]) match {
                case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                case Right(r) => r match {
                  case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                  case Right(v) => onComplete(api.update(v)) {
                    case Success(e) => complete(StatusCodes.BadRequest -> e.asJson.toString)
                    case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                  }
                }
              }
            }
          }
        }
      }
    } ~ delete {
      pathEnd {
        parameter('id.as[Long].?) {
          case Some(id) => onComplete(api.deleteOne(id)) {
            case Success(v) => complete(StatusCodes.OK -> v.toString)
            case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
          }
          case None => onComplete(api.deleteAll()) {
            case Success(v) => complete(StatusCodes.OK -> v.toString)
            case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
          }
        }
      }
    }
  }
}

object RestApiRoute {
  def apply(api: Api[Entity, Long]): RestApiRoute = new RestApiRoute(api)
}
