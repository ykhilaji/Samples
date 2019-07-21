package finagle.rpc

import com.twitter.finagle.{Service, http}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.apache.logging.log4j.LogManager

object FinagleHttpHandler {
  private val logger = LogManager.getLogger("FinagleHttpHandler")

  def apply(endpoint: CalculatorService.ServicePerEndpoint): Service[http.Request, http.Response] = (req: http.Request) => {
    if (req.containsParam("query")) {
      processQuery(req, endpoint)
    } else {
      logger.warn("Query param should be set")
      Future.value(http.Response(req.version, Status.BadRequest))
    }
  }

  private def processQuery(req: http.Request, endpoint: CalculatorService.ServicePerEndpoint): Future[http.Response] = {
    val query = req.getParam("query")
    logger.info(s"Execute query: $query")
    endpoint.task(CalculatorService.Task.Args(Request(query)))
      .map(taskResult => {
        val httpResponse = http.Response(req.version, Status.Ok)
        httpResponse.contentString = taskResult.result.toString
        httpResponse
      })
  }
}