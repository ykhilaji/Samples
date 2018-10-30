package basic

import akka.actor.{Actor, ActorSystem, Props, Timers}

import scala.concurrent.duration._

// Sample of convenient way to schedule messages in actor to it's self.
// TimerScheduler  - is not thread-safe, i.e. it must only be used within the actor that owns it.
class ActorWithTimer extends Actor with Timers {
  timers.startSingleTimer("Some-msg-key", "start", 1.second)

  override def receive: Receive = {
    case "start" =>
      println("Received start message")
      timers.startPeriodicTimer("Some-msg-key", "scheduled-message-body", 1.second)
    case msg =>
      println(s"Received scheduled message: $msg")
  }
}

object ActorTimerScheduler extends App {
  val system = ActorSystem("ActorTimerScheduler")
  val actor = system.actorOf(Props[ActorWithTimer])
}
