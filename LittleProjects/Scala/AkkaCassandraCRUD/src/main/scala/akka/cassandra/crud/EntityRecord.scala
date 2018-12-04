package akka.cassandra.crud

import com.outworkers.phantom.dsl._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future

case class EntityRecord(id: Long, hash: String, value: String)

object EntityRecord extends DefaultJsonProtocol {
  implicit val recordToJson: RootJsonFormat[EntityRecord] = jsonFormat3(EntityRecord.apply)

  def apply(id: Long, hash: String, value: String): EntityRecord = new EntityRecord(id, hash, value)
}

abstract class EntityTable extends Table[EntityTable, EntityRecord] {

  object id extends LongColumn with PartitionKey

  object hash extends StringColumn with ClusteringOrder with Ascending

  object value extends StringColumn with Index

  def findByIdAndHash(id: Long, hash: String): Future[Option[EntityRecord]] =
    select.where(_.id eqs id).and(_.hash eqs hash).one()

  def findByValue(value: String): Future[List[EntityRecord]] =
    select.allowFiltering().where(_.value eqs value).fetch()

  override def fromRow(row: Row): EntityRecord = EntityRecord(
    id(row),
    hash(row),
    value(row)
  )

  def store(entityRecord: EntityRecord): Future[ResultSet] =
    insert()
      .value(_.id, entityRecord.id)
      .value(_.hash, entityRecord.hash)
      .value(_.value, entityRecord.value)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
}
