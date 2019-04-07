package project

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import crud.{CrudService, Entity}
import org.apache.logging.log4j.LogManager
import org.apache.thrift.async.TAsyncClientManager
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.{TNonblockingSocket, TNonblockingTransport}
import project.service.EntityServiceNonBlocking

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//single threaded client
object NonBlockingClient extends App {
  val logger = LogManager.getLogger
  logger.info("Start client")

  val config = ConfigFactory.load().getConfig("crud.server")

  val protocol: TCompactProtocol.Factory = new TCompactProtocol.Factory()
  val asyncManager: TAsyncClientManager = new TAsyncClientManager()
  val transport: TNonblockingTransport = new TNonblockingSocket(config.getString("host"), config.getInt("port"))

  val client: CrudService.AsyncClient = new CrudService.AsyncClient(
    protocol,
    asyncManager,
    transport
  )

  val service = EntityServiceNonBlocking(client)

  val entity = new Entity()
  entity.id = 1
  entity.value = "value"
  entity.timestamp = System.currentTimeMillis()

  // only 1 call at any point of time
  Await.ready(service.remove(1), Duration(5, TimeUnit.SECONDS))
  Await.ready(service.create(entity), Duration(5, TimeUnit.SECONDS))
  val result = Await.result(service.findOne(1), Duration(5, TimeUnit.SECONDS))
  logger.info(s"Result: $result")
}
