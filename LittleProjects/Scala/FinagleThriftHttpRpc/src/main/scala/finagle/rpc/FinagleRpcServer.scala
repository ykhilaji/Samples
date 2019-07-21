package finagle.rpc

import com.twitter.finagle.ThriftMux
import com.twitter.finagle.thrift.{Protocols, RichServerParam}
import com.twitter.util.{Await, Future}
import com.typesafe.config.Config
import org.apache.logging.log4j.LogManager

private class CalculatorServiceImpl extends CalculatorService[Future] {
  import CalculatorServiceImpl._

  override def task(x: Request): Future[Response] = Future {
    logger.info(s"Process expression: ${x.expression}")
    ExpressionExecutor.execute(x.expression)
  }
    .map(result => {
      logger.info(s"Result: ${x.expression} = $result")
      Response(result)
    })
    .handle {
      case error =>
        logger.error(s"Error while processing expression: ${x.expression}. Return default result: 0", error)
        Response(0)
    }
}

private object CalculatorServiceImpl {
  private val logger = LogManager.getLogger(classOf[CalculatorServiceImpl])
}

class FinagleRpcServer(config: Config) {
  val finagledService = new CalculatorService.FinagledService(
    new CalculatorServiceImpl, RichServerParam(protocolFactory = Protocols.binaryFactory())
  )

  /*
    The serve method has two variants: one for instances of Service, and another for ServiceFactory.
    The ServiceFactory variants are used for protocols in which connection state is significant: a new Service is requested from the
    ServiceFactory for each new connection, and requests on that connection are dispatched to the supplied service.
    The service is also closed when the client disconnects or the connection is otherwise terminated.
 */
  val server = ThriftMux.server
      .withLabel("calculator-service")
      .serve(
        s":${config.getInt("port")}",
        finagledService
      )
}

object FinagleRpcServer {
  private val logger = LogManager.getLogger(classOf[FinagleRpcServer])

  def apply(config: Config): FinagleRpcServer = new FinagleRpcServer(config)
}