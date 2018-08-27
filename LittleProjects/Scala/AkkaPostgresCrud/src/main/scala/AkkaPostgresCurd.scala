object Model {

  import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
  import spray.json.{DefaultJsonProtocol, RootJsonFormat}

  case class Entity(id: Long = 0, value: String = "")

  object Entity extends SQLSyntaxSupport[Entity] {
    override def schemaName: Option[String] = Option("crud")

    override def tableName: String = "entity"

    override def columns: Seq[String] = Seq("id", "value")

    def apply(rs: WrappedResultSet): Entity = Entity(rs.get[Long](1), rs.get[String](2))
  }

  object Entity2Json extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val entity2Json: RootJsonFormat[Entity] = jsonFormat2(Entity.apply)
  }

}

object Configuration {

  import scalikejdbc._

  val settings = ConnectionPoolSettings.apply(
    initialSize = 5,
    maxSize = 10,
    connectionTimeoutMillis = 5000,
    driverName = "org.postgresql.Driver"
  )

  ConnectionPool.singleton(
    url = "jdbc:postgresql://192.168.99.100:5432/postgres",
    user = "postgres",
    password = "",
    settings = settings
  )

  def initDB(implicit session: DBSession = AutoSession) = DB localTx {
    implicit session => {
      sql"create schema if not exists crud".execute.apply()
      sql"create sequence if not exists crud.entity_seq".execute.apply()
      sql"create table if not exists crud.entity(id numeric(38) primary key default nextval('crud.entity_seq'), value varchar(255))".execute.apply()
    }
  }
}

object Repository {

  import Model.Entity
  import scalikejdbc._
  import scala.util.Try
  import scala.concurrent.ExecutionContext
  import scala.concurrent.Future

  trait RepositoryComponent[A, ID] {
    def repository: Repository

    trait Repository {
      def save(a: A): A

      def findById(id: ID): A

      def findAll(): Iterable[A]

      def exists(id: ID): Boolean

      def update(a: A): A

      def delete(a: A): Future[Try[Unit]]

      def deleteById(id: ID): Future[Try[Unit]]
    }

  }

  trait SQLRepositoryComponent extends RepositoryComponent[Entity, Long] {
    override def repository: Repository = new SQLRepository

    implicit val executionContext = ExecutionContext.global

    class SQLRepository extends Repository {
      override def save(a: Entity): Entity = DB localTx {
        implicit session => {
          val id = withSQL {
            QueryDSL.insert.into(Entity).namedValues(Entity.column.value -> a.value)
          }.update().apply()

          Entity(id, a.value)
        }
      }

      override def findById(id: Long): Entity = DB readOnly {
        implicit session => {
          withSQL {
            QueryDSL.select.from[Entity](Entity as Entity.syntax("e")).where.eq(Entity.column.id, id)
          }.map(Entity(_)).single().apply.getOrElse(Entity())
        }
      }

      override def findAll(): Iterable[Entity] = DB readOnly {
        implicit session => {
          withSQL {
            QueryDSL.select.from[Entity](Entity as Entity.syntax("a"))
          }.map(Entity(_)).list.apply()
        }
      }

      override def exists(id: Long): Boolean = DB readOnly {
        implicit session => {
          sql"select exists(select 1 from crud.entity where entity.id=$id)".map(rs => rs.get[Boolean](1)).single().apply().getOrElse(false)
        }
      }

      override def update(a: Entity): Entity = DB localTx {
        implicit session => {
          withSQL {
            QueryDSL.update(Entity).set(
              Entity.column.value -> a.value
            ).where.eq(Entity.column.id, a.id)
          }.update().apply()

          a
        }
      }

      override def delete(a: Entity): Future[Try[Unit]] = DB futureLocalTx[Try[Unit]] {
        implicit session => {
          Future {
            Try(
              withSQL {
                QueryDSL.delete.from(Entity).where.eq(Entity.column.id, a.id)
              }.update().apply()
            )
          }
        }
      }

      override def deleteById(id: Long): Future[Try[Unit]] = DB futureLocalTx[Try[Unit]] {
        implicit session => {
          Future {
            Try(
              withSQL {
                QueryDSL.delete.from(Entity).where.eq(Entity.column.id, id)
              }.update().apply()
            )
          }
        }
      }
    }

  }

}

object Service {

  import Model.Entity
  import Repository.SQLRepositoryComponent
  import scala.util.Try
  import scala.concurrent.Future

  trait ServiceComponent[A, ID] {
    def service: Service

    trait Service {
      def save(a: A): A

      def findById(id: ID): A

      def findAll(): Iterable[A]

      def exists(id: ID): Boolean

      def update(a: A): A

      def delete(a: A): Future[Try[Unit]]

      def deleteById(id: ID): Future[Try[Unit]]
    }

  }

  trait SQLServiceComponent extends ServiceComponent[Entity, Long] {
    this: SQLRepositoryComponent =>
    override def service: Service = new SQLService

    class SQLService extends Service {
      override def save(a: Entity): Entity = if (!exists(a.id)) {
        repository.save(a)
      } else {
        a
      }

      override def findById(id: Long): Entity = repository.findById(id)

      override def findAll(): Iterable[Entity] = repository.findAll()

      override def exists(id: Long): Boolean = if (id > 0) repository.exists(id) else false

      override def update(a: Entity): Entity = repository.update(a)

      override def delete(a: Entity): Future[Try[Unit]] = repository.delete(a)

      override def deleteById(id: Long): Future[Try[Unit]] = repository.deleteById(id)
    }

  }

}

object Application extends App {

  import scala.util.{Success, Failure}
  import Model.Entity
  import Model.Entity2Json._
  import Repository.SQLRepositoryComponent
  import Service.SQLServiceComponent
  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
  import akka.http.scaladsl.server.Directives._
  import akka.stream.ActorMaterializer
  import spray.json._
  import scala.io.StdIn

  Configuration.initDB()
  implicit val actorSystem = ActorSystem("crud")
  implicit val materializer = ActorMaterializer()
  implicit val context = actorSystem.dispatcher
  val service = (new SQLServiceComponent with SQLRepositoryComponent).service

  val route = path("api") {
    get {
      parameter('id.as[Long].?) {
        id => id.getOrElse(0L) match {
          case 0 => complete(HttpEntity(ContentTypes.`application/json`, service.findAll().toJson.toString))
          case x if x > 0 => complete(HttpEntity(ContentTypes.`application/json`, service.findById(x).toJson.toString))
          case _ => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Id should be a positive long number"))
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
          if (service.exists(entity.id)) {
            complete(HttpEntity(ContentTypes.`application/json`, service.update(entity).toJson.toString))
          } else {
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Entity with such id soes not exist"))
          }
      }
    } ~ delete {
      parameter('id.as[Long].?) {
        id =>
          if (service.exists(id.getOrElse(-1))) {
            onComplete(service.deleteById(id.getOrElse(-1))) {
              case Success(r) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Success"))
              case Failure(e) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, e.getLocalizedMessage))
            }
          } else {
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Entity with such id soes not exist"))
          }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())

}
