package finagle.rpc

import com.twitter.finagle._
import com.typesafe.config.Config
import org.apache.logging.log4j.LogManager

object FinagleRpcClient {
  private val logger = LogManager.getLogger("FinagleRpcClient")

  def apply(config: Config): CalculatorService.ServicePerEndpoint = ThriftMux.client.servicePerEndpoint[CalculatorService.ServicePerEndpoint](
    config.getString("server.host"),
    "calculator-service"
  )
}
