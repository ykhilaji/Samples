package akka

import akka.actor.{Actor, ActorSystem, Props}


trait One {
  this: Actor =>

  val oneBehavior: Receive = {
    case "one" =>
      println("One")
  }
}

trait Two {
  this: Actor =>

  val twoBehavior: Receive = {
    case "two" =>
      println("Two")
  }
}

class SomeActor extends Actor with One with Two {
  override def receive: Receive = oneBehavior.orElse(twoBehavior).orElse {
    case _ =>
      println("default behaviour")
  }
}


object ActorPartialFunctionChaining extends App {
  val system = ActorSystem("ActorPartialFunctionChaining")
  val a = system.actorOf(Props[SomeActor])

  a ! "msg"
  a ! "one"
  a ! "two"
}
