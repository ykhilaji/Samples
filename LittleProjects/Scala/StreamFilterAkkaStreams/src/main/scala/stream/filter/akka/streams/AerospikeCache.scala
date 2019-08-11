package stream.filter.akka.streams
import com.aerospike.client.async.{EventPolicy, NioEventLoops}
import com.aerospike.client.listener.{ExistsArrayListener, ExistsListener}
import com.aerospike.client.policy.ClientPolicy
import com.aerospike.client._
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future, Promise}

class AerospikeCache(config: Config) extends Cache {
  import AerospikeCache._

  private val eventLoopSize = Runtime.getRuntime.availableProcessors()
  private val concurrentMax = eventLoopSize * 40

  private val eventPolicy = new EventPolicy()
  eventPolicy.maxCommandsInProcess = 40
  eventPolicy.maxCommandsInQueue = 0 // no limit for queue

  private val eventLoops = new NioEventLoops(eventPolicy, eventLoopSize)

  private val client = {
    val hosts: Array[Host] = Host.parseHosts(config.getString("host"), 3000)

    val policy = new ClientPolicy()
    policy.eventLoops = eventLoops
    policy.maxConnsPerNode = concurrentMax

    new AerospikeClient(policy, hosts: _*)
  }
  private val namespace = config.getString("namespace")
  private val setName = config.getString("setName")

  sys.addShutdownHook({
    logger.info("Close aerospike client")
    client.close()
    logger.info("Aerospike client was closed")
  })

  override def isExist(entity: Entity): Boolean =
    client.exists(null, new Key(namespace, setName, entity.key))

  override def isExist(entities: immutable.Seq[Entity]): immutable.Seq[Boolean] = {
    val keys = entities.map(e => new Key(namespace, setName, e.key)).toArray
    immutable.Seq(client.exists(null, keys): _*)
  }

  override def isExistAsync(entity: Entity)(implicit ex: ExecutionContext): Future[Boolean] = {
    val next = eventLoops.next()
    val promise = Promise[Boolean]
    client.exists(next, new ExistsListener {
      override def onSuccess(key: Key, exists: Boolean): Unit = promise.success(exists)

      override def onFailure(exception: AerospikeException): Unit = {
        logger.error(s"Error while check existence of $entity", exception)
        promise.failure(exception)
      }
    }, null, new Key(namespace, setName, entity.key))

    promise.future
  }

  override def isExistAsync(entities: immutable.Seq[Entity])(implicit ex: ExecutionContext): Future[immutable.Seq[Boolean]] = {
    val next = eventLoops.next()
    val promise = Promise[immutable.Seq[Boolean]]
    val keys = entities.map(e => new Key(namespace, setName, e.key)).toArray
    client.exists(next, new ExistsArrayListener {
      override def onSuccess(keys: Array[Key], exists: Array[Boolean]): Unit =
        promise.success(immutable.Seq(exists: _*))

      override def onFailure(exception: AerospikeException): Unit = {
        logger.error(s"Error while check existence of $entities", exception)
        promise.failure(exception)
      }
    }, null, keys)

    promise.future
  }
}

object AerospikeCache {
  private val logger = LoggerFactory.getLogger(AerospikeCache.getClass)

  def apply(config: Config): AerospikeCache = {
    Log.setCallback(new LogHandler)
    new AerospikeCache(config)
  }

  private class LogHandler extends Log.Callback {
    override def log(level: Log.Level, message: String): Unit = level match {
      case Log.Level.DEBUG => logger.debug(message)
      case Log.Level.ERROR => logger.error(message)
      case Log.Level.INFO => logger.info(message)
      case Log.Level.WARN => logger.warn(message)
    }
  }
}