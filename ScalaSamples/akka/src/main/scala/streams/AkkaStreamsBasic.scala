package streams

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.ActorSystem

import scala.concurrent.Future

//Akka Streams does not send dropped stream elements to the dead letter office
//Akka Streams do not allow null to be passed through the stream as an element
//By default, Akka Streams will fuse the stream operators
//ActorMaterializer can be created inside separate actor using it's context

object AkkaStreamsBasic extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)

  // Run in single actor
  val done: Future[Done] = source
    .filter(_ % 2 == 0)
    .map(_ * 2)
    .filter(_ / 100 < 1)
    .runWith(Sink.foreach(println))

  done.onComplete(_ => system.terminate())
}

object AkkaStreamsBackPressure extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val done: Future[Done] = source
    .buffer(5, OverflowStrategy.dropBuffer)
    .map(i => {
      Thread.sleep(1000)
      i
    })
    .async // separate actor
    .runWith(Sink.foreach(println))
  // dropTail - 1 - 2 - 3 - 4 - 10
  // dropNew - 1 - 2 - 3 - 4 - 5
  // dropHead - 6 - 7 - 8 - 9 - 10
  // dropBuffer - 6 - 7 - 8 - 9 - 10
  done.onComplete(_ => system.terminate())
}

object AkkaStreamsMaterializedValues extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  // this is like low-level api for the same: source.filter(_ % 2 == 0).runWith(Sink.fold(0)(_ + _)
  val filter: Flow[Int, Int, NotUsed] = Flow[Int].filter(_ % 2 == 0)
  val sum: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)((acc, i) => acc + i)

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val graph: RunnableGraph[Future[Int]] = source
      .via(filter)
      .toMat(sum)(Keep.right)
  val run: Future[Int] = graph.run()
  run.onComplete(r => {
    println(s"Total ${r.getOrElse(0)}")
    system.terminate()
  })
}