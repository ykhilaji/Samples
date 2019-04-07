package project

import com.typesafe.config.ConfigFactory
import crud.CrudService
import org.apache.logging.log4j.LogManager
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TSocket
import project.service.EntityServiceSync

object SyncClient extends App {
  val logger = LogManager.getLogger("client")
  logger.info("Start client")

  val config = ConfigFactory.load().getConfig("crud.server")
  val transport = new TSocket(config.getString("host"), config.getInt("port"))
  val protocol = new TBinaryProtocol(transport)

  val client = new CrudService.Client(protocol)
  transport.open()
  val service = new EntityServiceSync(client)

  service.findOne(1)
}
