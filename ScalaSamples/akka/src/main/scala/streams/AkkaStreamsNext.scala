package streams

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, RunnableGraph, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

object AkkaStreamsFileSourceAndSink extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(System.getProperty("user.dir"), "input.txt"))
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(System.getProperty("user.dir"), "output.txt"))

  source.to(sink).run().onComplete(_ => system.terminate())
}

object AkkaStreamsFileIOFiltered extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get(System.getProperty("user.dir"), "input.txt"))
      val odd: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(System.getProperty("user.dir"), "odd.txt"))
      val even: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(System.getProperty("user.dir"), "even.txt"))
      val broadcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))

      source ~> Flow[ByteString].map(_.utf8String) ~> Flow[String].mapConcat(_.split(" ").toList) ~>
        Flow[String].map(_.toInt) ~> Flow[Int].map {i => println(i); i} ~> broadcast

      broadcast ~> Flow[Int].filter(_ % 2 == 0) ~> Flow[Int].map {i => println(s"Even: $i"); s"$i\n"} ~> Flow[String].map(ByteString(_)) ~> odd
      broadcast ~> Flow[Int].filter(_ % 2 != 0) ~> Flow[Int].map {i => println(s"Odd: $i"); s"$i\n"} ~> Flow[String].map(ByteString(_)) ~> even

      ClosedShape
  })

  graph.run()
  Thread.sleep(1000)
  system.terminate()
}

object AkkaStreamsCSVFormattedSource extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  FileIO.fromPath(Paths.get(System.getProperty("user.dir"), "csvinput.txt"))
    .via(Flow[ByteString].map {s => println(s"Input: $s"); s})
    .via(Framing.delimiter(ByteString("\n"), 4048).map(_.utf8String))
    .via(Flow[String].map {s => println(s"Mapped to string: $s"); s})
    .via(Flow[String].map {s =>
      val (a, b) = s.split(",").toList.splitAt(1)
      (a.head, b.head)
    })
    .runForeach(f => println(s"Left: ${f._1} Right: ${f._2}"))
    .onComplete(_ => system.terminate())
}