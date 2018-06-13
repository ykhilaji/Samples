package akka

import akka.actor.{Actor, ActorSystem, Props}

class A extends Actor {
  def stateOne: Receive = {
    case "change" =>
      println("From stateOne to stateTwo")
      context.become(stateTwo)
    case "unbecome" =>
      println("Go to default state")
      context.unbecome()
    case msg =>
      println(s"Got message in stateOne: $msg")
  }

  def stateTwo: Receive = {
    case "change" =>
      println("From stateTwo to stateOne")
      context.become(stateOne)
    case "unbecome" =>
      println("Go to default state")
      context.unbecome()
    case msg =>
      println(s"Got message in stateTwo: $msg")
  }

  override def receive: Receive = {
    case "one" =>
      println("Change state -> stateOne")
      context.become(stateOne)
    case "two" =>
      println("Change state -> stateTwo")
      context.become(stateTwo)
    case msg =>
      println(s"Got simple message: $msg")
  }
}

object ActorBecomeUnbecome extends App {
  val system = ActorSystem("ActorBecomeUnbecome")
  val a = system.actorOf(Props[A])

  a ! "message"
  a ! "one"
  a ! "message"
  a ! "change"
  a ! "message"
  a ! "unbecome"
  a ! "message"
}
