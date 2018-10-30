package basic

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}

object Messages extends Enumeration {
  val PING, PONG = Value
}

class Ping(pong: ActorRef) extends Actor {
  var counter = 10

  pong ! Messages.PING

  override def receive: Receive = {
    case Messages.PONG =>
      if (counter > 0) {
        println("Received PONG")
        counter -= 1
        sender() ! Messages.PING
      } else {
        sender() ! PoisonPill
      }
  }
}

class Pong extends Actor {

  override def receive: Receive = {
    case Messages.PING =>
      println("Received PING")
      sender() ! Messages.PONG
  }
}

object ActorPingPongSample extends App {
  val system = ActorSystem("PingPong")
  val pong = system.actorOf(Props[Pong])
  val ping = system.actorOf(Props(classOf[Ping], pong))
}
