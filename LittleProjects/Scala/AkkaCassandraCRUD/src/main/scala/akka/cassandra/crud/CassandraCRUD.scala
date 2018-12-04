package akka.cassandra.crud

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http

object CassandraCRUD extends App {
  implicit val system = ActorSystem("cassandraCrud")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val bindingFuture = Http().bindAndHandle(Rest.api, "localhost", 8080)
  println("Started")
}
