object Model {

  import scalikejdbc._
  import spray.json.{DefaultJsonProtocol, RootJsonFormat}
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

  case class Entity(id: Long = 0, value: String = "")

  object Entity extends SQLSyntaxSupport[Entity] {
    override def schemaName: Option[String] = Option("crud")

    override def tableName: String = "entity"

    override def columnNames: Seq[String] = Seq("id", "value")

    def apply(rs: WrappedResultSet): Entity = Entity(rs.get[Long](1), rs.get[String](2))
  }

  object JsonWrapper extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val entity2json: RootJsonFormat[Entity] = jsonFormat2(Entity.apply)
  }

}

object Service {

  import Repository._
  import Model._

  trait ServiceComponent[A, ID] {
    def service: Service

    trait Service {
      def save(a: A): A

      def update(a: A): A

      def delete(id: ID): String

      def get(id: ID): A

      def getAll(): Iterable[A]

      def exists(id: ID): Boolean
    }

  }

  trait EntityServiceComponent extends ServiceComponent[Entity, Int] {
    this: RepositoryComponent[Entity, Int] =>
    override def service: Service = new EntityService()

    class EntityService extends Service {
      override def save(a: Entity): Entity = repository.save(a)

      override def update(a: Entity): Entity = repository.update(a)

      override def delete(id: Int): String = repository.delete(id)

      override def get(id: Int): Entity = repository.get(id)

      override def getAll(): Iterable[Entity] = repository.getAll()

      override def exists(id: Int): Boolean = repository.exists(id)
    }

  }

}


object Repository {

  import scalikejdbc._
  import scala.util.{Failure, Success, Try}
  import Model._

  object DataSource {
    val settings = ConnectionPoolSettings(
      initialSize = 3,
      maxSize = 5,
      connectionTimeoutMillis = 5000,
      driverName = "com.mysql.jdbc.Driver"
    )

    ConnectionPool.singleton(
      url = "jdbc:mysql://192.168.99.100:3306/crud",
      user = "root",
      password = "root",
      settings = settings
    )

    def initDB(implicit session: DBSession = AutoSession): Unit = {
      sql"CREATE TABLE IF NOT EXISTS entity (id INT AUTO_INCREMENT PRIMARY KEY, value VARCHAR(255))".execute.apply()
    }
  }

  // cake-pattern sample
  trait RepositoryComponent[A, ID] {
    def repository: Repository

    trait Repository {
      def save(a: A): A

      def update(a: A): A

      def delete(id: ID): String

      def get(id: ID): A

      def getAll(): Iterable[A]

      def exists(id: ID): Boolean
    }

  }

  trait MySQLEntityRepositoryComponent extends RepositoryComponent[Entity, Int] {
    override def repository: Repository = new MySQLRepository()

    class MySQLRepository extends Repository {
      implicit val session = AutoSession
      private val entityAlias = Entity.syntax("entity")

      override def save(a: Entity): Entity = DB localTx {
        implicit session => {
          val id = withSQL {
            QueryDSL.insert.into(Entity).namedValues(Entity.column.value -> a.value)
          }.update.apply()

          Entity(id, a.value)
        }
      }

      override def update(a: Entity): Entity = DB localTx {
        implicit session => {
          withSQL {
            QueryDSL.update(Entity).set(Entity.column.value -> a.value).where.eq(Entity.column.id, a.id)
          }.update.apply()

          a
        }
      }

      override def delete(id: Int): String = DB localTx {
        implicit session => {
          Try(withSQL {
            QueryDSL.delete.from(Entity).where.eq(Entity.column.id, id)
          }.update.apply()) match {
            case Success(_) => "Success"
            case Failure(_) => "Failure"
          }
        }
      }

      override def get(id: Int): Entity = DB readOnly {
        implicit session =>
          withSQL {
            QueryDSL.select.from(Entity as entityAlias).where.eq(Entity.column.id, id)
          }.map(Entity(_)).single().apply().getOrElse[Entity](new Entity())
      }

      override def getAll(): Iterable[Entity] = DB readOnly {
        implicit session =>
          withSQL {
            QueryDSL.select.from(Entity as entityAlias)
          }.map(Entity(_)).list.apply()
      }

      override def exists(id: Int): Boolean = DB readOnly {
        implicit session =>
          sql"SELECT exists(SELECT 1 FROM crud.entity WHERE id=${id} LIMIT 1)"
            .map(rs => rs.get[Boolean](1)).single.apply().getOrElse(false)
      }
    }

  }

}

object Application extends App {

  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
  import akka.http.scaladsl.server.Directives._
  import akka.stream.ActorMaterializer

  import scala.io.StdIn
  import Model._
  import Model.JsonWrapper._
  import Repository._
  import Service._
  import spray.json._

  Repository.DataSource.initDB()
  val service = (new EntityServiceComponent with MySQLEntityRepositoryComponent).service
  implicit val actorSystem = ActorSystem("crud")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val route = path("api") {
    get {
      parameters('id.as[Int].?) {
        id => {
          id.getOrElse(0) match {
            case 0 => complete(HttpEntity(ContentTypes.`application/json`, service.getAll().toJson.toString))
            case x if x > 0 => complete(HttpEntity(ContentTypes.`application/json`, service.get(x).toJson.toString))
            case _ => complete(HttpEntity(ContentTypes.`application/json`, "Error. ID should be an integer"))
          }
        }
      }
    } ~ post {
      entity(as[Entity]) {
        entity => {
          complete(HttpEntity(ContentTypes.`application/json`, service.update(entity).toJson.toString))
        }
      }
    } ~ put {
      entity(as[Entity]) {
        entity => {
          complete(HttpEntity(ContentTypes.`application/json`, service.save(entity).toJson.toString))
        }
      }
    } ~ delete {
      parameters('id.as[Int]) {
        id => {
          if (service.exists(id)) {
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, service.delete(id)))
          } else {
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Entity with id: $id does not exist"))
          }
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
