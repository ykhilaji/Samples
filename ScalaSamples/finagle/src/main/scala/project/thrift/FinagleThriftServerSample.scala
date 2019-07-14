package project.thrift

import com.twitter.finagle.{ListeningServer, Thrift}
import com.twitter.util.{Await, Future}
import finagleThriftSample.{Request, Response}

object FinagleThriftServerSample {
  def main(args: Array[String]): Unit = {
    val server: ListeningServer = Thrift.server.serveIface(
      "localhost:1234",
      new finagleThriftSample.RPCService[Future] {
        override def task(x: Request): Future[Response] = {
          val response = finagleThriftSample.Response(s"Result: ${x.body} from request: ${x.id}")
          Future.value(response)
        }

        override def ping(): Future[Unit] = Future(println("Ping"))
      }
    )

    Await.ready(server)
  }
}
