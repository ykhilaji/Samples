package project.http

import com.twitter.finagle.http.path.{/, Integer, Root}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.{Http, Service, SimpleFilter}
import com.twitter.util.{Await, Future}

object FinagleHttpServerSample {
  def main(args: Array[String]): Unit = {
    val filterMethods = new SimpleFilter[Request, Response] {
      override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
        println(s"New request: $request")
        request.method match {
          case Method.Get => service(request)
          case _ => Future.value(Response(request.version, Status.Forbidden))
        }
      }
    }

    val handleEceptions = new SimpleFilter[Request, Response] {
      override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
        service(request).handle {
          case error =>
            val response = Response(request.version, Status.BadRequest)
            response.setContentString(s"Error message: ${error.getLocalizedMessage}")
            response
        }
      }
    }

    def firstService(id: Int) = new Service[Request, Response] {
      def apply(req: Request): Future[Response] = {
        val response = Response(req.version, Status.Ok)
        response.setContentString(s"Response from first service: $id")
        Future.value(response)
      }
    }

    def secondService(id: Int) = new Service[Request, Response] {
      def apply(req: Request): Future[Response] = {
        val response = Response(req.version, Status.Ok)
        response.setContentString(s"Response from second service: $id")
        Future.value(response)
      }
    }

    val router = RoutingService.byPathObject[Request] {
      case Root / "first" / Integer(id) => firstService(id)
      case Root / "second" / Integer(id) => secondService(id)
      case _ => throw new IllegalArgumentException("Incorrect request")
    }

    val fullService = filterMethods andThen handleEceptions andThen router

    val server = Http.serve(":8080", fullService)
    Await.ready(server)
  }
}
