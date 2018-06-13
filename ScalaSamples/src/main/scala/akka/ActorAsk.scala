package akka

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import scala.util.{Success, Failure}

import scala.concurrent.duration.Duration

class AskActor extends Actor {
  override def receive: Receive = {
    case msg =>
      try {
        println(s"Received: $msg")
        sender() ! "reply"
      } catch {
        case e: Exception =>
          println(s"Error: $e")
          sender() ! akka.actor.Status.Failure(e) // important - akka does not do this automatically
      }
  }
}

object ActorAsk extends App {
  val system = ActorSystem("ActorAsk")
  val actor = system.actorOf(Props[AskActor])
  implicit val dispatcher = system.dispatcher

  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))

  val response = actor ? "some message"
  response.onComplete {
    case scala.util.Failure(e) => println(e)
    case Success(msg) => println(s"Response: $msg")
  }
}
