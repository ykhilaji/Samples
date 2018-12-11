package akka.mongo.crud.web

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.mongo.crud.service.WebService
import spray.json._
import JsonFormats._
import akka.http.scaladsl.model.StatusCodes
import org.mongodb.scala.bson.ObjectId

import scala.util.{Failure, Success, Try}

class HttpListener(webService: WebService) {
  val api: Route = pathPrefix("api") {
    pathPrefix("event") {
      put {
        entity(as[String]) {
          e =>
            parseEvent(e) match {
              case Success(value) => onComplete(webService.saveEvent(value)) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
              }
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
        }
      } ~ post {
        entity(as[String]) {
          e =>
            parseEvent(e) match {
              case Success(value) => onComplete(webService.updateEvent(value)) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
              }
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
        }
      } ~ delete {
        entity(as[String]) {
          e =>
            parseObjectId(e) match {
              case Success(value) => onComplete(webService.deleteEvent(value)) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
              }
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
        }
      } ~ get {
        path("type" / Segment) { `type` =>
          onComplete(webService.countEventsByType(`type`)) {
            case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        } ~ path("source" / Segment) {source =>
          parseObjectId(source) match {
            case Success(id) => onComplete(webService.findAllEventsByEventSource(id)) {
              case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      }
    } ~ pathPrefix("source") {
      put {
        entity(as[String]) {
          e =>
            parseEventSource(e) match {
              case Success(value) => onComplete(webService.saveEventSource(value)) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
              }
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
        }
      } ~ post {
        entity(as[String]) {
          e =>
            parseEventSource(e) match {
              case Success(value) => onComplete(webService.updateEventSource(value)) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
              }
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
        }
      } ~ delete {
        entity(as[String]) {
          e =>
            parseObjectId(e) match {
              case Success(value) => onComplete(webService.deleteEventSource(value)) {
                case Success(_) => complete(StatusCodes.OK)
                case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
              }
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
        }
      } ~ get {
        pathEndOrSingleSlash {
          onComplete(webService.findAllEventSources()) {
            case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        } ~ path("name" / Segment) { name =>
          onComplete(webService.findEventSourceByName(name)) {
            case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        } ~ path("pattern" / Segment) { pattern =>
          onComplete(webService.findEventSourceByRegex(pattern)) {
            case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      }
    }
  }

  private def parseEvent(body: String): Try[EventRequest] = Try(body.parseJson.convertTo[EventRequest])

  private def parseEventSource(body: String): Try[EventSourceRequest] = Try(body.parseJson.convertTo[EventSourceRequest])

  private def parseObjectId(body: String): Try[ObjectId] = Try(body.parseJson.convertTo[ObjectId])
}

object HttpListener {
  def apply(webService: WebService): HttpListener = new HttpListener(webService)
}