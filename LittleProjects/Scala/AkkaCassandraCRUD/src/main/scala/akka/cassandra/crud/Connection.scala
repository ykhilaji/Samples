package akka.cassandra.crud

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.builder.query.CreateQuery.Default
import com.outworkers.phantom.connectors.ContactPoint
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

object Connection {
  val cassandra: CassandraConnection = ContactPoint
    .local
    .withClusterBuilder(_.addContactPoint("192.168.99.100")
      .withPort(9042)
      .withSocketOptions(new SocketOptions()
        .setConnectTimeoutMillis(5000)
        .setReadTimeoutMillis(10000)))
    .keySpace(KeySpace("cassandra_crud")
      .ifNotExists()
      .`with`(replication eqs SimpleStrategy.replication_factor(1))
      .and(durable_writes eqs true))
}

class CassandraCrudDatabase(override val connector: CassandraConnection) extends Database[CassandraCrudDatabase](connector) {

  object entityTable extends EntityTable with Connector {
    override def tableName: String = "entities"

    override def autocreate(keySpace: KeySpace): Default[EntityTable, EntityRecord] = create.ifNotExists()(keySpace)
  }
}

trait CassandraCrudDatabaseProvider extends DatabaseProvider[CassandraCrudDatabase]

trait EntityService extends CassandraCrudDatabaseProvider {
  def findByIdAndHash(id: Long, hash: String): Future[Option[EntityRecord]] = db.entityTable.findByIdAndHash(id, hash)

  def findByValue(value: String): Future[List[EntityRecord]] = db.entityTable.findByValue(value)

  def save(entity: EntityRecord): Future[ResultSet] = db.entityTable.store(entity)

  def delete(id: (Long, String)): Future[ResultSet] = db.entityTable.delete().where(_.id eqs id._1).and(_.hash eqs id._2).future()

  def update(entity: EntityRecord): Future[ResultSet] =
    db.entityTable.update().where(_.id eqs entity.id).and(_.hash eqs entity.hash).modify(_.value setTo entity.value).future()
}