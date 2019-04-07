package project

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import crud.CrudService
import org.apache.logging.log4j.LogManager
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.server.TNonblockingServer
import org.apache.thrift.transport.{TFramedTransport, TNonblockingServerSocket}
import project.repository.EntityRepository
import project.service.CrudServiceAsyncImpl

// single threaded server
object NonBlockingServer extends App {
  val logger = LogManager.getLogger
  logger.info("Start server")

  val config = ConfigFactory.load().getConfig("crud")
  val repository = EntityRepository[IO](config.getConfig("aerospike"))
  val rpc: CrudServiceAsyncImpl = new CrudServiceAsyncImpl(repository)

  val processor: CrudService.AsyncProcessor[CrudServiceAsyncImpl] = new CrudService.AsyncProcessor(rpc)
  val transport = new TNonblockingServerSocket(config.getConfig("server").getInt("port"))
  val server = new TNonblockingServer(new TNonblockingServer.Args(transport)
  .transportFactory(new TFramedTransport.Factory())
  .protocolFactory(new TCompactProtocol.Factory())
  .processor(processor))

  sys.addShutdownHook(server.stop())
  server.serve()
}
