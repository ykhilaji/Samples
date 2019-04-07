package project

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import crud.CrudService
import org.apache.logging.log4j.LogManager
import org.apache.thrift.server.{TServer, TSimpleServer}
import org.apache.thrift.transport.TServerSocket
import project.repository.EntityRepository
import project.service.CrudServiceSyncImpl

object SyncServer extends App {
  val logger = LogManager.getLogger("server")
  logger.info("Start server")
  val config = ConfigFactory.load().getConfig("crud")
  val repository = new EntityRepository[IO](config.getConfig("aerospike"))
  val rpc = new CrudServiceSyncImpl(repository)

  val processor = new CrudService.Processor(rpc)
  val transport = new TServerSocket(config.getConfig("server").getInt("port"))
  val server = new TSimpleServer(new TServer.Args(transport).processor(processor))

  sys.addShutdownHook(server.stop())
  server.serve()
}
