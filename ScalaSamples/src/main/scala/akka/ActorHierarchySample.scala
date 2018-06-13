package akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case class MessageFromParent(msg: String)
case class MessageToChild(msg: String)
case class MessageFromChild(msg: String)

class ParentWithChild extends Actor {
  val child = this.context.actorOf(Props[Child])

  override def receive: Receive = {
    case MessageFromParent(msg) =>
      println(s"Parent with child got message: $msg")
      child forward MessageToChild(msg)
  }
}

class Parent(parentWithChild: ActorRef) extends Actor {
  parentWithChild ! MessageFromParent("some message")

  override def receive: Receive = {
    case MessageFromChild(msg) => println(s"Message from child: $msg")
  }
}

class Child extends Actor {
  override def receive: Receive = {
    case MessageToChild(_) => sender() ! MessageFromChild("Response from child")
  }
}

object ActorHierarchySample extends App {
  val system = ActorSystem("ActorHierarchySample")
  val parentWithChild = system.actorOf(Props[ParentWithChild])
  val parent = system.actorOf(Props(classOf[Parent], parentWithChild))
}
