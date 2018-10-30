package basic

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration._

object ActorScheduling extends App {
  class First extends Actor {
    override def receive: Receive = {
      case request =>
        println(s"Received scheduled request: $request")
        sender() ! "some response"
    }
  }

  class Second(first: ActorRef) extends Actor {
    implicit val dispatcher = context.system.dispatcher
    context.system.scheduler.schedule(0.second, 1.second, first, "request-message-body")

    override def receive: Receive = {
      case response =>
        println(s"Received response: $response")
    }
  }

  val system = ActorSystem("ActorScheduling")
  val first = system.actorOf(Props[First])
  val second = system.actorOf(Props(classOf[Second], first))
}
