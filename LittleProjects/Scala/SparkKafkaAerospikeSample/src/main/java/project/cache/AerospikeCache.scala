package project.cache

import com.aerospike.client.{AerospikeClient, Bin, Key, Record}
import com.aerospike.client.policy.{ClientPolicy, Policy, WritePolicy}
import com.typesafe.config.Config
import org.apache.logging.log4j.LogManager
import project.model.EventInfo

class AerospikeCache(conf: Config) extends Cache {
  @transient
  private lazy val client: AerospikeClient = {
    val clientPolicy = new ClientPolicy()
    new AerospikeClient(clientPolicy, conf.getString("host"), conf.getInt("port"))
  }

  @transient
  private val logger = LogManager.getLogger("AerospikeCache")

  private val namespace: String = conf.getString("namespace")

  private val setname: String = conf.getString("setname")

  override def get(key: Long): Option[EventInfo] = {
    val readPolicy = new Policy()
    logger.info(s"Get event state by key: $key")
    client.get(readPolicy, new Key(namespace, setname, key)) match {
      case null => None
      case record => Some(fromRecord(record))
    }
  }

  override def put(key: Long, value: EventInfo): Unit = {
    val writePolicy = new WritePolicy()
    logger.info(s"Put event state: $value")
    client.put(writePolicy, new Key(namespace, setname, key), bins(value): _*)
  }

  private def bins(value: EventInfo): Seq[Bin] = Seq(
    new Bin("id", value.id),
    new Bin("count", value.count),
    new Bin("startTime", value.startTime),
    new Bin("endTime", value.endTime)
  )

  private def fromRecord(record: Record): EventInfo = EventInfo(
    record.getLong("id"),
    record.getLong("count"),
    record.getLong("startTime"),
    record.getLong("endTime")
  )
}

object AerospikeCache {
  def apply(conf: Config): AerospikeCache = new AerospikeCache(conf)
}