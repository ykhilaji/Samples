package project.http

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}

object FinagleHttpClientSample {
  def main(args: Array[String]): Unit = {
    val client: Service[http.Request, http.Response] = Http.newService(":8080")
    val request = http.Request(http.Method.Get, "/first/1")
    val response: Future[http.Response] = client(request)

    response.onFailure(err => println(err.getLocalizedMessage))
    response.onSuccess(resp => println("GET success: " + resp))

    Await.ready(response)
  }
}
