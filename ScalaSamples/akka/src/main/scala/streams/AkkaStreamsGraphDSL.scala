package streams

import java.util.concurrent.TimeUnit

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object AkkaStreamsGraphDSL extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    {
      implicit builder: GraphDSL.Builder[NotUsed] => {
        import GraphDSL.Implicits._

        val source: Source[Int, NotUsed] = Source(1 to 10)
        val oddEven = builder.add(Broadcast[Int](2))
        val merge = builder.add(Merge[String](2))
        val out = Sink.foreach(println)

        val oddFilter = Flow[Int].filter(_ % 2 != 0)
        val evenFilter = Flow[Int].filter(_ % 2 == 0)

        val oddToString: Flow[Int, String, NotUsed] = Flow[Int].map(i => s"Odd: $i")
        val evenToString: Flow[Int, String, NotUsed] = Flow[Int].map(i => s"Even: $i")

        source ~> oddEven ~> oddFilter ~> oddToString ~> merge ~> out
                  oddEven ~> evenFilter ~> evenToString ~> merge

        ClosedShape
      }
    }
  })

  val x = graph.run()
}

object AkkaStreamsGraphDSLActorSink extends App {
  class A extends Actor {
    override def receive: Receive = {
      case "onComplete" => context.system.terminate()
      case x => println(s"Received: $x")
    }
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val a = system.actorOf(Props[A])

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    {
      implicit builder: GraphDSL.Builder[NotUsed] => {
        import GraphDSL.Implicits._

        val source: Source[Int, NotUsed] = Source(1 to 10)
        val toActor = Sink.actorRef(a, "onComplete")
        source ~> toActor
        ClosedShape
      }
    }
  })

  val x = graph.run()
}

object AkkaStreamsGraphDSLActorSinkWithResponse extends App {
  class A extends Actor {
    override def receive: Receive = {
      case 10 => context.system.terminate()
      case x =>
        println(s"Received: $x")
        sender() ! x
    }
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  implicit val askTimeout = Timeout(1, TimeUnit.SECONDS)

  val a = system.actorOf(Props[A])

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    {
      implicit builder: GraphDSL.Builder[NotUsed] => {
        import GraphDSL.Implicits._

        val source: Source[Int, NotUsed] = Source(1 to 10)
        source ~> Flow[Int].ask(2)(a) ~> Sink.foreach[String](println)
        ClosedShape
      }
    }
  })

  val x = graph.run()
}


object AkkaStreamGraphCycle extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    {
      implicit builder: GraphDSL.Builder[NotUsed] => {
        import GraphDSL.Implicits._

        val source: Source[Int, NotUsed] = Source(1 to 10)
        val bc = builder.add(Broadcast[Int](2))
        val merge = builder.add(Merge[Int](2))

        source ~> merge ~> Flow[Int].map{i => println(s"Input: $i"); i} ~> Flow[Int].filter(_ > 0) ~> bc ~> Sink.foreach[Int](i => println(s"Output: $i"))
                  merge <~ Flow[Int].map{i => println(s"Cycled: $i");  i - 1} <~ Flow[Int].buffer(10, OverflowStrategy.dropHead) <~ bc
        ClosedShape
      }
    }
  })

  val x = graph.run()
}

object AkkaStreamGraphTick extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    {
      implicit builder: GraphDSL.Builder[NotUsed] => {
        import GraphDSL.Implicits._

        Source.tick[Int](FiniteDuration(0, TimeUnit.SECONDS), FiniteDuration(1, TimeUnit.SECONDS), 0) ~>
          Flow[Int].map(_ => Random.nextInt(10)) ~>
          Sink.foreach(println)

        ClosedShape
      }
    }
  })

  val x = graph.run()
}