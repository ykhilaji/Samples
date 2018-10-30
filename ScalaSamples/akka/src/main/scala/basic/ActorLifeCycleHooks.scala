package basic

import akka.actor.{Actor, ActorSystem, Props}

class Sample extends Actor {
  override def preStart(): Unit = println("Pre start")

  override def postStop(): Unit = println("Post stop")

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = println("Pre restart")

  override def postRestart(reason: Throwable): Unit = println("Post restart")

  override def receive: Receive = {
    case msg => println(msg)
  }
}

object ActorLifeCycleHooks extends App {
  val system = ActorSystem("ActorLifeCycleHooks")
  val actor = system.actorOf(Props[Sample])

  actor ! "some message"
}
