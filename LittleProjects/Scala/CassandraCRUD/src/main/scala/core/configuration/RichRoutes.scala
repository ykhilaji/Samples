package core.configuration


import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import core.model.User
import core.service.RichUserServiceComponent
import org.slf4j.{Logger, LoggerFactory}
import core.model.JsonWrapper._
import spray.json._

import scala.util.{Failure, Success, Try}

class RichRoutes(component: RichUserServiceComponent) {
  private val logger: Logger = LoggerFactory.getLogger("AkkaCassandraCRUD")
  private val service = component.richServiceOperations

  private val index = {
    pathEndOrSingleSlash {
      get {
        getFromResource("html/index.html")
      }
    }
  }

  private val crud = {
    pathPrefix("api" / "users") {
      pathEnd {
        get {
          parameters('email.as[String].?) {
            case Some(email) =>
              logger.info(s"Get user by email: $email")
              Try(service.selectAsync(email).get(1, TimeUnit.SECONDS)) match {
                case Success(user) => complete(JsObject(("status", JsString("ok")), ("result", user.toJson)))
                case Failure(ex) => complete(JsObject(("status", JsString("error")), ("exception", JsString(ex.getMessage))))
              }
            case _ =>
              logger.info("Get all users (slow method)")
              Try(service.selectJson()) match {
                case Success(result) => complete(JsObject(("status", JsString("ok")), ("result", result)))
                case Failure(ex) => complete(JsObject(("status", JsString("error")), ("exception", JsString(ex.getMessage))))
              }
          }
        } ~ post {
          entity(as[User]) {
            entity =>
              logger.info(s"Update user: $entity")
              Try(service.updateAsync(entity)) match {
                case Success(_) => complete(JsObject(("status", JsString("ok"))))
                case Failure(ex) => complete(JsObject(("status", JsString("error")), ("exception", JsString(ex.getMessage))))
              }
          }
        } ~ put {
          entity(as[User]) {
            entity =>
              logger.info(s"Insert user: $entity")
              Try(service.insertAsync(entity)) match {
                case Success(_) => complete(JsObject(("status", JsString("ok"))))
                case Failure(ex) => complete(JsObject(("status", JsString("error")), ("exception", JsString(ex.getMessage))))
              }
          }
        } ~ delete {
          entity(as[JsValue]) {
            email =>
              logger.info(s"Delete user by email: $email")
              Try(service.deleteAsync(email.asJsObject.getFields("email").head.asInstanceOf[JsString].value)) match {
                case Success(_) => complete(JsObject(("status", JsString("ok"))))
                case Failure(ex) => complete(JsObject(("status", JsString("error")), ("exception", JsString(ex.getMessage))))
              }
          }
        }
      } ~ path(Segment) {
        firstName =>
          get {
            logger.info(s"Get users by firstName: $firstName")
            Try(service.selectByFirstNameJson(firstName)) match {
              case Success(result) => complete(JsObject(("status", JsString("ok")), ("result", result)))
              case Failure(ex) => complete(JsObject(("status", JsString("error")), ("exception", JsString(ex.getMessage))))
            }
          }
      }
    }
  }

  private val staticResources = {
    get {
      getFromResourceDirectory("public/")
    }
  }

  val route: Route = index ~ crud ~ staticResources
}
