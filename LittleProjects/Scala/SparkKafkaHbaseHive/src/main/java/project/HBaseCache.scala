package project

import com.typesafe.config.Config
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.logging.log4j.LogManager

class HBaseCache(config: Config) extends Cache {
  @transient
  private lazy val logger = LogManager.getLogger("hbaseCache")

  @transient
  private lazy val connection: Connection = {
    val configuration = HBaseConfiguration.create()
    ConnectionFactory.createConnection(configuration)
  }

  @transient
  private lazy val hTable: Table = connection
    .getTable(TableName.valueOf(config.getString("hbase.tableName")))

  override def get(key: Long): Option[Entity] = {
    logger.info(s"Get entity by key: $key")
    val get = new Get(Bytes.toBytes(key))
    val result = hTable.get(get)

    if (!result.isEmpty) {
      Some(Entity(
        id = key,
        value = Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("value"))),
        timestamp = Bytes.toLong(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("timestamp")))
      ))
    } else {
      None
    }
  }

  override def put(key: Long, entity: Entity): Unit = {
    logger.info(s"Put entity: $entity")
    val put = new Put(Bytes.toBytes(key))
    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("value"), Bytes.toBytes(entity.value))
    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("timestamp"), Bytes.toBytes(entity.timestamp))

    hTable.put(put)
  }

  override def remove(key: Long): Unit = {
    logger.info(s"Remove entity by key: $key")
    val delete = new Delete(Bytes.toBytes(key))
    hTable.delete(delete)
  }
}

object HBaseCache {
  def apply(config: Config): HBaseCache = new HBaseCache(config) with MeasurableCache
}
