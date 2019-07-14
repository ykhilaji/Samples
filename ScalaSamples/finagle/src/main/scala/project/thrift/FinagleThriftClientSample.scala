package project.thrift

import com.twitter.finagle.Thrift
import com.twitter.util.Await

object FinagleThriftClientSample {
  def main(args: Array[String]): Unit = {
    val clientServicePerEndpoint: finagleThriftSample.RPCService.ServicePerEndpoint =
      Thrift.client.servicePerEndpoint[finagleThriftSample.RPCService.ServicePerEndpoint](
        "localhost:1234",
        "thrift_client"
      )

    clientServicePerEndpoint.ping(finagleThriftSample.RPCService.Ping.Args())
    val result = clientServicePerEndpoint.task(finagleThriftSample.RPCService.Task.Args(finagleThriftSample.Request(1, "task")))

    Await.result(result.map(println(_)))
  }
}
