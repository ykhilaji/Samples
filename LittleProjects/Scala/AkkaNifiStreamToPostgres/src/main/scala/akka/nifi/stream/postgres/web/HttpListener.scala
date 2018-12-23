package akka.nifi.stream.postgres.web

import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.nifi.stream.postgres.model.Entity
import akka.nifi.stream.postgres.web.HttpListener.NewEvent
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import JsonFormatter._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import io.circe.{Decoder, ParsingFailure}

import scala.util.{Failure, Success}

class HttpListener(restApiService: RestApiService)(implicit system: ActorSystem, materializer: ActorMaterializer) {
  val api: Route = pathPrefix("api") {
    pathPrefix("new") {
      path("event") {
        post {
          pathEndOrSingleSlash {
            requestEntityEmpty {
              complete(StatusCodes.BadRequest -> "empty body")
            } ~ requestEntityPresent {
              entity(as[HttpEntity]) {
                body =>
                  onComplete(body
                    .dataBytes
                    .via(Flow[ByteString].map(_.utf8String))
                    .via(Flow[String].map(parse(_).map(_.as[NewEvent])))
                    .via(Flow[Either[ParsingFailure, Decoder.Result[NewEvent]]].map(_.right.get.right.get)) // =\
                    .via(Flow[NewEvent].map(restApiService.newEvent))
                    .toMat(Sink.head[Either[Throwable, Unit]])(Keep.right)
                    .run()) {
                    case Success(e) => e match {
                      case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                      case Right(_) => complete(StatusCodes.OK)
                    }
                    case Failure(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                  }
              }
            }
          }
        }
      } ~ path("entity") {
        post {
          pathEndOrSingleSlash {
            requestEntityEmpty {
              complete(StatusCodes.BadRequest -> "empty body")
            } ~ requestEntityPresent {
              entity(as[String]) {
                body => {
                  parse(body).map(_.as[Entity]) match {
                    case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                    case Right(v) => v.map(restApiService.saveEntity) match {
                      case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                      case Right(r) => r match {
                        case Left(err) => complete(StatusCodes.BadRequest -> err.getLocalizedMessage)
                        case Right(rr) => complete(StatusCodes.OK -> rr.asJson.toString)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } ~ path("find" / IntNumber) {
      id =>
        parameter('f.as[Boolean].?(false)) {
          f => {
            get {
              if (f) {
                restApiService.findEntityWithNoEvents(id) match {
                  case Left(e) => complete(StatusCodes.BadRequest -> e.getLocalizedMessage)
                  case Right(v) => complete(StatusCodes.OK -> v.asJson.toString)
                }
              } else {
                restApiService.findEntityWithNoUnknownEvents(id) match {
                  case Left(e) => complete(StatusCodes.BadRequest -> e.getLocalizedMessage)
                  case Right(v) => complete(StatusCodes.OK -> v.asJson.toString)
                }
              }
            }
          }
        }
    }
  }

  def start(): Unit = {
    Http().bindAndHandle(api, "localhost", 8080)
  }
}

object HttpListener {
  case class NewEvent(id: Long)

  def apply(restApiService: RestApiService = DefaultRestService())(implicit system: ActorSystem, materializer: ActorMaterializer): HttpListener =
    new HttpListener(restApiService)
}
