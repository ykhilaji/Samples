import java.util.concurrent.TimeUnit

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{DataType, Session}
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import com.datastax.driver.mapping.{Mapper, MappingManager}
import core.model.User
import core.repository.{UserAccessor, UserCassandraRepositoryComponent}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._

import scala.collection.JavaConverters._

class RepositoryTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  val session: Session = EmbeddedCassandraServerHelper.getSession
  lazy val manager = new MappingManager(session)
  lazy val component = new UserCassandraRepositoryComponent {
    override val userMapper: Mapper[User] = manager.mapper(classOf[User])
  }
  lazy val repository: component.CrudRepository = component.repository
  lazy val accessor: UserAccessor = manager.createAccessor(classOf[UserAccessor])

  override protected def beforeAll(): Unit = {
    session.execute(SchemaBuilder
      .createKeyspace("users")
      .ifNotExists()
      .`with`().durableWrites(true)
      .replication(Map[String, AnyRef]("class" -> "SimpleStrategy", "replication_factor" -> 1.asInstanceOf[AnyRef]).asJava))
    session.execute(SchemaBuilder
      .createType("users", "address")
      .ifNotExists()
      .addColumn("country", DataType.text())
      .addColumn("city", DataType.text())
      .addColumn("street", DataType.text()))
    session.execute(SchemaBuilder
      .createType("users", "contact")
      .ifNotExists()
      .addColumn("code", DataType.text())
      .addColumn("number", DataType.text()))
    session.execute(SchemaBuilder
      .createTable("users", "user")
      .ifNotExists()
      .addPartitionKey("email", DataType.text())
      .addClusteringColumn("firstName", DataType.text())
      .addColumn("lastName", DataType.text())
      .addColumn("age", DataType.cint())
      .addUDTListColumn("contacts", SchemaBuilder.frozen("users.contact"))
      .addUDTColumn("address", SchemaBuilder.udtLiteral("users.address")))
    session.execute(SchemaBuilder
      .createIndex("userFirstNameIndex")
      .ifNotExists()
      .onTable("users", "user")
      .andColumn("firstName"))
  }

  override protected def afterAll(): Unit = {
    session.execute(SchemaBuilder.dropTable("users", "user").ifExists())
    session.execute(SchemaBuilder.dropType("users", "contact").ifExists())
    session.execute(SchemaBuilder.dropType("users", "address").ifExists())
    session.execute(SchemaBuilder.dropKeyspace("users").ifExists())

    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

  override protected def beforeEach(): Unit = {
    session.execute(QueryBuilder.truncate("users", "user"))
  }

  test("Insert new user") {
    val user = new User("e1", "f1", "l1", 10)
    repository.insert(user)
    repository.select("e1") shouldEqual user
  }

  test("Delete user") {
    val user = new User("e1", "f1", "l1", 10)
    repository.insert(user)

    repository.select("e1") shouldEqual user
    repository.delete("e1")
    repository.select("e1") shouldEqual null
  }

  test("Select all users") {
    val u1 = new User("e1", "f1", "l1", 10)
    val u2 = new User("e2", "f2", "l2", 10)
    val u3 = new User("e3", "f3", "l3", 10)

    repository.insert(u1)
    repository.insert(u2)
    repository.insert(u3)

    accessor.select().get(1, TimeUnit.SECONDS).all().asScala.toList shouldEqual List(u3, u1, u2)
  }


  test("Select users by firtsname") {
    val u1 = new User("e1", "f1", "l1", 10)
    val u2 = new User("e2", "f2", "l2", 10)
    val u3 = new User("e3", "f3", "l3", 10)

    repository.insert(u1)
    repository.insert(u2)
    repository.insert(u3)

    accessor.selectByFirstName("f1").get(1, TimeUnit.SECONDS).all().asScala shouldEqual List(u1)
  }
}

