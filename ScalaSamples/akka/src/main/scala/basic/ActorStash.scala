package basic

import akka.actor.{Actor, ActorSystem, Props, Stash}

// Also, there is an UnboundedStash
class B extends Actor with Stash {
  def anotherState: Receive = {
    case msg =>
      println(s"Received: $msg")
  }

  override def receive: Receive = {
    case "change" =>
      unstashAll()
      context.become(anotherState)
    case _ =>
      stash()
  }
}

object ActorStash extends App {
  val system = ActorSystem("ActorStash")
  val b = system.actorOf(Props[B])

  b ! "msg1"
  b ! "msg2"
  b ! "msg3"
  b ! "change"
}
