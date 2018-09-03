import scala.util.{Failure, Success, Try}
import scala.concurrent.{ExecutionContext, Future}

object Model {

  import spray.json.{DefaultJsonProtocol, RootJsonFormat}
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

  case class Entity(id: Long = 0, value: String = "")

  object Entity {
    def apply(x: Map[String, String]): Entity = new Entity(x.getOrElse("id", "0").toLong, x.getOrElse("value", ""))
  }

  object Entity2Json extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val entity2json: RootJsonFormat[Entity] = jsonFormat2(Entity.apply)
  }

}

object Repository {

  trait RepositoryComponent[T, ID] {
    def repository: Repository

    trait Repository {
      def getById(id: ID): T

      def deleteById(id: ID): String

      def save(entity: T): T

      def update(entity: T): T

      def exists(id: ID): Boolean

      def getByIdAsync(id: ID)(implicit context: ExecutionContext): Future[T]

      def deleteByIdAsync(id: ID)(implicit context: ExecutionContext): Future[String]

      def saveAsync(entity: T)(implicit context: ExecutionContext): Future[T]

      def updateAsync(entity: T)(implicit context: ExecutionContext): Future[T]

      def existsAsync(id: ID)(implicit context: ExecutionContext): Future[Boolean]
    }

  }

  import Model.Entity

  trait RedisRepositoryComponent extends RepositoryComponent[Entity, Long] {
    override def repository: Repository = new RedisRepository()

    class RedisRepository extends Repository {

      import com.redis._
      import serialization._
      import Parse.Implicits._

      private val client = new RedisClientPool(host = "192.168.99.100", port = 6379)

      override def getById(id: Long): Entity = client.withClient {
        client => {
          Entity.apply(client.hmget[String, String](id, "id", "value").getOrElse(Map.empty[String, String]))
        }
      }

      override def deleteById(id: Long): String = Try {
        client.withClient {
          client => {
            client.watch(id)
            client.del(id)
            client.unwatch()
            "ok"
          }
        }
      } match {
        case Success(r) => "ok"
        case Failure(e) => e.getLocalizedMessage
      }

      override def save(entity: Entity): Entity = client.withClient {
        client => {
          val nextId = client.incr("ids").get
          client.hset(nextId, "id", nextId)
          client.hset(nextId, "value", entity.value)
          Entity(nextId, entity.value)
        }
      }

      override def update(entity: Entity): Entity = client.withClient {
        client => {
          client.watch(entity.id)
          client.hset(entity.id, "value", entity.value)
          client.unwatch()
          entity
        }
      }

      override def exists(id: Long): Boolean = client.withClient {
        client => {
          client.watch(id)
          val exists = client.exists(id)
          client.unwatch()
          exists
        }
      }

      override def getByIdAsync(id: Long)(implicit context: ExecutionContext): Future[Entity] = Future {
        client.withClient {
          client => {
            Entity.apply(client.hmget[String, String](id, "id", "value").getOrElse(Map.empty[String, String]))
          }
        }
      }

      override def deleteByIdAsync(id: Long)(implicit context: ExecutionContext): Future[String] = Future {
        Try {
          client.withClient {
            client => {
              client.watch(id)
              client.del(id)
              client.unwatch()
              "ok"
            }
          }
        } match {
          case Success(r) => "ok"
          case Failure(e) => e.getLocalizedMessage
        }
      }

      override def saveAsync(entity: Entity)(implicit context: ExecutionContext): Future[Entity] = Future {
        client.withClient {
          client => {
            val nextId = client.incr("ids").get
            client.hset(nextId, "id", nextId)
            client.hset(nextId, "value", entity.value)
            Entity(nextId, entity.value)
          }
        }
      }

      override def updateAsync(entity: Entity)(implicit context: ExecutionContext): Future[Entity] = Future {
        client.withClient {
          client => {
            client.watch(entity.id)
            client.hset(entity.id, "value", entity.value)
            client.unwatch()
            entity
          }
        }
      }

      override def existsAsync(id: Long)(implicit context: ExecutionContext): Future[Boolean] = Future {
        client.withClient {
          client => {
            client.watch(id)
            val exists = client.exists(id)
            client.unwatch()
            exists
          }
        }
      }
    }

  }

}

object Service {

  trait ServiceComponent[T, ID] {
    def service: Service

    trait Service {
      def getById(id: ID): T

      def deleteById(id: ID): String

      def save(entity: T): T

      def update(entity: T): T

      def exists(id: ID): Boolean

      def getByIdAsync(id: ID)(implicit context: ExecutionContext): Future[T]

      def deleteByIdAsync(id: ID)(implicit context: ExecutionContext): Future[String]

      def saveAsync(entity: T)(implicit context: ExecutionContext): Future[T]

      def updateAsync(entity: T)(implicit context: ExecutionContext): Future[T]

      def existsAsync(id: ID)(implicit context: ExecutionContext): Future[Boolean]
    }

  }

  import Repository.RepositoryComponent

  trait DefaultServiceComponent[T, ID] extends ServiceComponent[T, ID] {
    this: RepositoryComponent[T, ID] =>
    override def service: Service = new DefaultService

    class DefaultService extends Service {
      override def getById(id: ID): T = repository.getById(id)

      override def deleteById(id: ID): String = repository.deleteById(id)

      override def save(entity: T): T = repository.save(entity)

      override def update(entity: T): T = repository.update(entity)

      override def exists(id: ID): Boolean = repository.exists(id)

      override def getByIdAsync(id: ID)(implicit context: ExecutionContext): Future[T] = repository.getByIdAsync(id)

      override def deleteByIdAsync(id: ID)(implicit context: ExecutionContext): Future[String] = repository.deleteByIdAsync(id)

      override def saveAsync(entity: T)(implicit context: ExecutionContext): Future[T] = repository.saveAsync(entity)

      override def updateAsync(entity: T)(implicit context: ExecutionContext): Future[T] = repository.updateAsync(entity)

      override def existsAsync(id: ID)(implicit context: ExecutionContext): Future[Boolean] = repository.existsAsync(id)
    }

  }

}

object Controller {

  import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
  import akka.http.scaladsl.server.Directives._
  import spray.json._
  import Model.Entity
  import Model.Entity2Json._
  import Service.DefaultServiceComponent
  import Repository.RedisRepositoryComponent

  private val service = (new DefaultServiceComponent[Entity, Long] with RedisRepositoryComponent).service

  def syncRoute = path("api") {
    get {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0L =>
              if (service.exists(id)) {
                complete(HttpEntity(ContentTypes.`application/json`, service.getById(x).toJson.toString))
              } else {
                complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Entity with such id does not exist"))
              }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    } ~ put {
      entity(as[Entity]) {
        entity =>
          complete(HttpEntity(ContentTypes.`application/json`, service.save(entity).toJson.toString))
      }
    } ~ post {
      entity(as[Entity]) {
        entity =>
          complete(HttpEntity(ContentTypes.`application/json`, service.update(entity).toJson.toString))
      }
    } ~ delete {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0L =>
              if (service.exists(id)) {
                complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, service.deleteById(x)))
              } else {
                complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Entity with such id does not exist"))
              }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    }
  }

  def asyncRoute(implicit context: ExecutionContext) = path("api" / "async") {
    get {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0L =>
              onComplete(service.existsAsync(id)) {
                case Success(r) if r => onComplete(service.getByIdAsync(x)) {
                  case Success(e) => complete(HttpEntity(ContentTypes.`application/json`, e.toJson.toString))
                  case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
                }
                case Success(r) if !r => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Entity with such id does not exist"))
                case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
              }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    } ~ put {
      entity(as[Entity]) {
        entity =>
          onComplete(service.saveAsync(entity)) {
            case Success(e) => complete(HttpEntity(ContentTypes.`application/json`, e.toJson.toString))
            case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
          }
      }
    } ~ post {
      entity(as[Entity]) {
        entity =>
          onComplete(service.updateAsync(entity)) {
            case Success(e) => complete(HttpEntity(ContentTypes.`application/json`, e.toJson.toString))
            case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
          }
      }
    } ~ delete {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0L =>
              onComplete(service.existsAsync(id)) {
                case Success(r) if r => onComplete(service.deleteByIdAsync(x)) {
                  case Success(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e))
                  case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
                }
                case Success(r) if !r => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Entity with such id does not exist"))
                case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
              }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    }
  }
}

object Application extends App {

  import akka.http.scaladsl.Http
  import akka.actor.ActorSystem
  import akka.stream.ActorMaterializer
  import akka.http.scaladsl.server.Directives._ // for '~'
  import scala.io.StdIn

  implicit val actorSystem = ActorSystem("crud")
  implicit val materializer = ActorMaterializer()
  implicit val context = actorSystem.dispatcher

  val route = Controller.syncRoute ~ Controller.asyncRoute
  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())

}