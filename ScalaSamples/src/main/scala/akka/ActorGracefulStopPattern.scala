package akka

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.pattern.gracefulStop

class First extends Actor {
  override def postStop(): Unit = println("Actor was stopped")

  override def receive: Receive = {
    case msg =>
      println(s"Got message: $msg")
  }
}

class Second(ref: ActorRef) extends Actor {
  implicit val dispatcher = context.system.dispatcher

  override def receive: Receive = {
    case "stop" =>
      gracefulStop(ref, 1.second, PoisonPill ).onComplete {
        case Success(result) =>
          println(s"Result of gracefulStop: $result")
        case Failure(e) =>
          println(e)
      }
  }
}

class Watcher(ref: ActorRef) extends Actor {
  context.watch(ref)

  override def receive: Receive = {
    case Terminated(`ref`) =>
      println("Watched actor was terminated")
  }
}


object ActorGracefulStopPattern extends App {
  val system = ActorSystem("ActorGracefulStopPattern")
  val first = system.actorOf(Props[First])
  val second = system.actorOf(Props(classOf[Second], first))
  val watcher = system.actorOf(Props(classOf[Watcher], first))

  second ! "stop"
}
