import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import net.spy.memcached.CachedData
import net.spy.memcached.transcoders.Transcoder
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success, Try}

object Model {

  case class Entity(id: Long = 0, value: String = "")

  object Entity {
    def apply(e: Entity): Entity = e
  }

  object Entity2Json extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val entity2json = jsonFormat2(Entity.apply)
  }

  class EntityTranscoder extends Transcoder[Entity] {
    override def encode(o: Entity): CachedData = {
      val buffer = ByteBuffer.allocate(getMaxSize)
      buffer.putLong(o.id)
      buffer.put(o.value.getBytes(StandardCharsets.UTF_8))
      new CachedData(0, buffer.array(), getMaxSize)
    }

    override def asyncDecode(d: CachedData): Boolean = {
      decode(d)
      true
    }

    override def decode(d: CachedData): Entity = {
      if (d.getData.length != 0) {
        val buffer = ByteBuffer.wrap(d.getData)

        val id = buffer.getLong
        val value = new String(buffer.get(new Array[Byte](buffer.remaining())).array(), StandardCharsets.UTF_8)
        val entity = Entity(id, value)
        entity
      } else {
        Entity()
      }
    }

    override def getMaxSize: Int = 1024 // 8 + 1016 (long + utf-8 chars)
  }

}

object Repository {

  trait RepositoryComponent[T, ID] {
    def repository: Repository

    trait Repository {
      def get(id: ID): T

      def save(t: T): T

      def update(t: T): T

      def delete(id: ID): String

      def getAsync(id: ID): Future[T]

      def saveAsync(t: T): Future[T]

      def updateAsync(t: T): Future[T]

      def deleteAsync(id: ID): Future[String]
    }

  }

  import Model.{Entity, EntityTranscoder}
  import net.spy.memcached.MemcachedClient
  import Configuration._

  implicit def javaFuture2ScalaFuture[A](javaFuture: java.util.concurrent.Future[A]): Future[A] = Future {
    javaFuture.get(500, TimeUnit.MILLISECONDS)
  }

  trait MemcachedCacheRepository extends RepositoryComponent[Entity, Long] {
    override def repository: Repository = new CacheRepository

    class CacheRepository extends Repository {
      private val client = new MemcachedClient(new InetSocketAddress("192.168.99.100", 11211))
      override def get(id: Long): Entity = client.get[Entity](id.toString, new EntityTranscoder)

      override def save(t: Entity): Entity = {
        val nextId = client.incr("next_ids", 1, 0, 0)
        val e = Entity(nextId, t.value)
        client.set[Entity](nextId.toString, 0, e, new EntityTranscoder).get()

        e
      }

      override def update(t: Entity): Entity = {
        client.set[Entity](t.id.toString, 0, t, new EntityTranscoder).get()
        t
      }

      override def delete(id: Long): String = {
        Try {
          client.delete(id.toString).get()
        } match {
          case Success(_) => "ok"
          case Failure(e) => e.getLocalizedMessage
        }
      }

      override def getAsync(id: Long): Future[Entity] = client.asyncGet[Entity](id.toString, new EntityTranscoder)

      override def saveAsync(t: Entity): Future[Entity] = Future {
        val nextId = client.asyncIncr("next_ids", 1, 0, 0).get(500, TimeUnit.MILLISECONDS)
        val e = Entity(nextId, t.value)
        client.set[Entity](nextId.toString, 0, e, new EntityTranscoder).get(500, TimeUnit.MILLISECONDS)
        e
      }

      override def updateAsync(t: Entity): Future[Entity] = Future {
        client.set[Entity](t.id.toString, 0, t, new EntityTranscoder).get(500, TimeUnit.MILLISECONDS)
        t
      }

      override def deleteAsync(id: Long): Future[String] = Future {
        Try {
          client.delete(id.toString).get(500, TimeUnit.MILLISECONDS)
        } match {
          case Success(_) => "ok"
          case Failure(e) => e.getLocalizedMessage
        }
      }
    }

  }

}

object Service {

  trait ServiceComponent[T, ID] {
    def service: Service

    trait Service {
      def get(id: ID): T

      def save(t: T): T

      def update(t: T): T

      def delete(id: ID): String

      def getAsync(id: ID): Future[T]

      def saveAsync(t: T): Future[T]

      def updateAsync(t: T): Future[T]

      def deleteAsync(id: ID): Future[String]
    }

  }

  import Repository.RepositoryComponent

  class DefaultServiceComponent[T, ID] extends ServiceComponent[T, ID] {
    this: RepositoryComponent[T, ID] =>

    override def service: Service = new DefaultService

    class DefaultService extends Service {
      override def get(id: ID): T = repository.get(id)

      override def save(t: T): T = repository.save(t)

      override def update(t: T): T = repository.update(t)

      override def delete(id: ID): String = repository.delete(id)

      override def getAsync(id: ID): Future[T] = repository.getAsync(id)

      override def saveAsync(t: T): Future[T] = repository.saveAsync(t)

      override def updateAsync(t: T): Future[T] = repository.updateAsync(t)

      override def deleteAsync(id: ID): Future[String] = repository.deleteAsync(id)
    }

  }

}

object Configuration {
  implicit val actorSystem = ActorSystem("crud")
  implicit val context: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()
}

object Route {

  import Configuration._
  import akka.http.scaladsl.server.Directives._
  import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
  import Model.Entity
  import Model.Entity2Json._
  import spray.json._
  import Service.DefaultServiceComponent
  import Repository.MemcachedCacheRepository

  val service = (new DefaultServiceComponent[Entity, Long] with MemcachedCacheRepository).service

  val syncRoute = path("api" / "sync") {
    get {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0 =>
              val result = service.get(id)
              if (result != null) {
                complete(HttpEntity(ContentTypes.`application/json`, result.toJson.toString))
              } else {
                complete(HttpEntity(ContentTypes.`application/json`, Entity().toJson.toString))
              }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    } ~ post {
      entity(as[Entity]) {
        entity => complete(HttpEntity(ContentTypes.`application/json`, service.update(entity).toJson.toString))
      }
    } ~ delete {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0 => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, service.delete(id)))
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    } ~ put {
      entity(as[Entity]) {
        entity => complete(HttpEntity(ContentTypes.`application/json`, service.save(entity).toJson.toString))
      }
    }
  }

  val asyncRoute = path("api" / "async") {
    get {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0 => onComplete(service.getAsync(id)) {
              case Success(e) =>
                if (e != null) {
                  complete(HttpEntity(ContentTypes.`application/json`, e.toJson.toString))
                } else {
                  complete(HttpEntity(ContentTypes.`application/json`, Entity().toJson.toString))
                }
              case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
            }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    } ~ post {
      entity(as[Entity]) {
        entity => onComplete(service.updateAsync(entity)) {
          case Success(e) => complete(HttpEntity(ContentTypes.`application/json`, e.toJson.toString))
          case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
        }
      }
    } ~ delete {
      parameter('id.as[Long]) {
        id =>
          id match {
            case x if x > 0 => onComplete(service.deleteAsync(id)) {
              case Success(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e))
              case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
            }
            case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Incorrect id"))
          }
      }
    } ~ put {
      entity(as[Entity]) {
        entity => onComplete(service.saveAsync(entity)) {
          case Success(e) => complete(HttpEntity(ContentTypes.`application/json`, e.toJson.toString))
          case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
        }
      }
    }
  }

  val route = syncRoute ~ asyncRoute
}

object AkkaMemcachedCrud extends App {
  import akka.http.scaladsl.Http
  import scala.io.StdIn
  import Configuration._

  val bindingFuture = Http().bindAndHandle(Route.route, "localhost", 8080)
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())
}
