package akka

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Stash}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuiteLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

class AkkaTesting extends TestKit(ActorSystem("test")) with FunSuiteLike with Matchers
  with ImplicitSender with BeforeAndAfter with BeforeAndAfterAll {
  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)



  test("echo actor") {
    class A extends Actor {
      override def receive: Receive = {
        case msg => sender() ! msg
      }
    }

    val a = system.actorOf(Props(new A))
    a ! "test"
    expectMsg(FiniteDuration(1, TimeUnit.SECONDS), "test")
  }

  test("actor termination") {
    class A extends Actor {
      override def receive: Receive = {
        case msg => sender() ! msg
      }
    }
    val a = system.actorOf(Props(new A))
    watch(a)
    a ! PoisonPill
    expectTerminated(a, Duration(1, TimeUnit.SECONDS))
  }

  test("actor scheduling") {
    class A(val ref: ActorRef) extends Actor {
      implicit val dispatcher = this.context.dispatcher
      this.context.system.scheduler.schedule(Duration(0, TimeUnit.SECONDS), Duration(1, TimeUnit.SECONDS), ref, "test")

      override def receive: Receive = {
        case msg => sender() ! msg
      }
    }
    val a = system.actorOf(Props(new A(self)))
    expectMsg(FiniteDuration(1, TimeUnit.SECONDS), "test")
  }

  test("actor ask") {
    import akka.pattern.ask
    class A extends Actor {
      override def receive: Receive = {
        case msg => sender() ! msg
      }
    }
    val a = system.actorOf(Props(new A))
    implicit val dispatcher = system.dispatcher
    implicit val timeout: Timeout = Timeout.apply(1, TimeUnit.SECONDS)
    val response = a ? "msg"
    Await.result(response, Duration(1, TimeUnit.SECONDS))
    assert(response.value.get.get == "msg")
  }

  test("stash | unstash messages") {
    class A extends Actor with Stash {
      override def receive: Receive = {
        case x: Int if x < 5 => stash()
        case x: Int if x >= 5 =>
          unstashAll()
          context.become(unstashState)
      }

      def unstashState: Receive = {
        case msg => sender() ! msg
      }
    }

    val test = TestProbe()
    val a = system.actorOf(Props(new A))
    test.send(a, 1)
    test.send(a, 2)
    test.send(a, 3)
    test.send(a, 4)
    test.expectNoMessage(FiniteDuration(1, TimeUnit.SECONDS))
    test.send(a, 5)
    test.expectMsgAllOf(FiniteDuration(1, TimeUnit.SECONDS), 1, 2, 3, 4)
  }

  test("become | unbecome pattern") {
    class A extends Actor {
      override def receive: Receive = a

      def a: Receive = {
        case "b" => this.context.become(b)
        case _ => sender() ! "a"
      }

      def b: Receive = {
        case "a" => this.context.become(a)
        case _ => sender() ! "b"
      }
    }

    val test = TestProbe()
    val a = system.actorOf(Props(new A))
    test.send(a, 1)
    test.send(a, 2)
    test.expectMsgAllOf(FiniteDuration(1, TimeUnit.SECONDS), "a", "a")
    test.send(a, "b")
    test.send(a, 1)
    test.send(a, 2)
    test.expectMsgAllOf(FiniteDuration(1, TimeUnit.SECONDS), "b", "b")
  }
}
