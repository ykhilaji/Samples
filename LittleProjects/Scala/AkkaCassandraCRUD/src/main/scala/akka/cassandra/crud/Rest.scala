package akka.cassandra.crud

import akka.cassandra.crud.Requests.PK
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import com.outworkers.phantom.connectors.RootConnector
import spray.json._

import scala.util.{Failure, Success, Try}

object Requests extends DefaultJsonProtocol {
  case class PK(id: Long, hash: String)

  implicit val pkToJson: RootJsonFormat[PK] = jsonFormat2(PK.apply)
}

object Rest {

  object CrudDatabase extends CassandraCrudDatabase(Connection.cassandra)

  trait CrudDatabaseProvider extends CassandraCrudDatabaseProvider {
    override def database: CassandraCrudDatabase = CrudDatabase
  }

  // it would be better to pass this as class parameter
  val entityService = new EntityService with CrudDatabaseProvider {}

  val api: Route = pathPrefix("api") {
    path("save") {
      put {
        entity(as[String]) {
          body => Try(body.parseJson.convertTo[EntityRecord]) match {
            case Success(value) => onComplete(entityService.save(value)) {
              case Success(_) => complete(StatusCodes.OK)
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      }
    } ~ path("delete") {
      delete {
        entity(as[String]) {
          body => Try(body.parseJson.convertTo[PK]) match {
            case Success(value) => onComplete(entityService.delete(value.id, value.hash)) {
              case Success(_) => complete(StatusCodes.OK)
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      }
    } ~ path("update") {
      post {
        entity(as[String]) {
          body => Try(body.parseJson.convertTo[EntityRecord]) match {
            case Success(value) => onComplete(entityService.save(value)) {
              case Success(_) => complete(StatusCodes.OK)
              case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
            }
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      }
    } ~ pathPrefix("select") {
      path(LongNumber / Segment) {
        (id, hash) => get {
          onComplete(entityService.findByIdAndHash(id, hash)) {
            case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      } ~ path(Segment) {
        value => get {
          onComplete(entityService.findByValue(value)) {
            case Success(value) => complete(StatusCodes.OK -> value.toJson.toString)
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getLocalizedMessage)
          }
        }
      }
    }
  }
}
