package akka

import akka.actor.{Actor, ActorSystem, Props, Terminated}

object ActorStatusMonitoringWatcher extends App {
  class SomeActor extends Actor {
    override def receive: Receive = {
      case msg => println(s"Received: $msg")
    }
  }

  class Watcher extends Actor {
    val child = this.context.actorOf(Props[SomeActor])
    this.context.watch(child)
    var lastSender = this.context.system.deadLetters

    override def receive: Receive = {
      case "terminate" => this.context.stop(child)
      case Terminated(`child`) => println("Child was terminated")
      case msg => child ! msg // just for sample
    }
  }

  val system = ActorSystem("ActorStatusMonitoringWatcher")
  val watcher = system.actorOf(Props[Watcher])

  watcher ! "some message"
  watcher ! "terminate"
}
