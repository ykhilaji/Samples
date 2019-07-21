package finagle.rpc

import com.twitter.finagle.Http
import com.twitter.util.Await
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.logging.log4j.LogManager

object FinagleRpc {
  private val logger = LogManager.getLogger("FinagleRpc")

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load().getConfig("rpc")

    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: jar (client | server)")
    }

    args(0) match {
      case "client" => startClient(config)
      case "server" => startServer(config)
    }
  }

  private def startClient(config: Config): Unit = {
    logger.info("Starting client")

    val endpoint = FinagleRpcClient(config)
    val httpService = FinagleHttpHandler(endpoint)
    val port = config.getInt("http.port")

    logger.info(s"Client is listening $port port")
    Await.result(Http.serve(s":$port", httpService))
  }

  private def startServer(config: Config): Unit = {
    logger.info("Starting thrift server")
    val server = FinagleRpcServer(config)
    Await.result(server.server)
    logger.info("Thrift server was started")
  }
}
