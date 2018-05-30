package core.configuration

import java.util.Collections

import com.datastax.driver.core.{Cluster, DataType, Session}
import com.datastax.driver.core.schemabuilder.{SchemaBuilder, UDTType}
import com.datastax.driver.mapping.{Mapper, MappingManager}
import core.configuration.listeners.{SchemaChangeListener, StateListener}
import core.model.User
import core.repository.UserAccessor

import scala.collection.JavaConverters._

object AppConfiguration {
  val cluster: Cluster = {
    val cluster = Cluster
      .builder()
      .addContactPoint("192.168.99.100")
      .withPort(9042)
      .withInitialListeners(Collections.singletonList(new StateListener))
      .build()

    val userKeyspace = SchemaBuilder
      .createKeyspace("users")
      .ifNotExists()
      .`with`().durableWrites(true)
      .replication(Map[String, AnyRef]("class" -> "SimpleStrategy", "replication_factor" -> 1.asInstanceOf[AnyRef]).asJava)

    val addressType = SchemaBuilder
      .createType("users", "address")
      .ifNotExists()
      .addColumn("country", DataType.text())
      .addColumn("city", DataType.text())
      .addColumn("street", DataType.text())

    val contactType = SchemaBuilder
      .createType("users", "contact")
      .ifNotExists()
      .addColumn("code", DataType.text())
      .addColumn("number", DataType.text())

    val userTable = SchemaBuilder
      .createTable("users", "user")
      .ifNotExists()
      .addPartitionKey("email", DataType.text())
      .addColumn("firstName", DataType.text())
      .addColumn("lastName", DataType.text())
      .addColumn("age", DataType.cint())
      .addUDTListColumn("contacts", SchemaBuilder.frozen("users.contact"))
      .addUDTColumn("address", SchemaBuilder.udtLiteral("users.address"))

    val userFirstNameIndex = SchemaBuilder
      .createIndex("userFirstNameIndex")
      .ifNotExists()
      .onTable("users", "user")
      .andColumn("firstName")

    val session = cluster.connect()

    //
    session.execute(SchemaBuilder.dropTable("users", "user").ifExists())
    session.execute(SchemaBuilder.dropType("users", "contact").ifExists())
    session.execute(SchemaBuilder.dropType("users", "address").ifExists())
    session.execute(SchemaBuilder.dropKeyspace("users").ifExists())

    session.execute(userKeyspace)
    session.execute(addressType)
    session.execute(contactType)
    session.execute(userTable)
    session.execute(userFirstNameIndex)

    cluster
      .register(new SchemaChangeListener)
  }

  val session: Session = cluster.connect()
  val manager = new MappingManager(session)

  val userMapper: Mapper[User] = manager.mapper(classOf[User])
  val userAccessor: UserAccessor = manager.createAccessor(classOf[UserAccessor])
}
