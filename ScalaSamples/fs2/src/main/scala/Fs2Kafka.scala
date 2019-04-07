import java.util.concurrent.TimeUnit

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import fs2._

import scala.concurrent.duration._
import scala.util.Random

object Fs2KafkaConsumer extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val consumerSettings =
      ConsumerSettings[String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("fs2-kafka")

    val stream = consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("source"))
      .flatMap(_.stream)
      .map { message =>
        println(message.record.value())
        message.committableOffset
      }
      .through(commitBatch)

    stream.compile.drain.as(ExitCode.Success)
  }
}

object Fs2KafkaConsumerGroupAdjacentBy extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val consumerSettings =
      ConsumerSettings[String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("fs2-kafka")

    import cats.implicits.catsKernelStdOrderForBoolean
    // group adjacent odd/even digits
    val stream = consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("source"))
      .flatMap(_.stream)
      .map(message => (message.record.value().toInt, message.committableOffset))
      .groupAdjacentBy(_._1 % 2 == 0) // import cats.implicits.catsKernelStdOrderForBoolean
      .map(r => {
      val list: Seq[(Int, CommittableOffset[IO])] = r._2.toList
      println(list) // List((7,CommittableOffset(source-0 -> 34)), (3,CommittableOffset(source-0 -> 35)), (1,CommittableOffset(source-0 -> 36)))
      list.last._2
    })
      .through(commitBatch)

    stream.compile.drain.as(ExitCode.Success)
  }
}

object Fs2KafkaConsumerWindow extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val consumerSettings =
      ConsumerSettings[String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("fs2-kafka")

    val stream = consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("source"))
      .flatMap(_.stream)
      .map(message => (message.record.value().toInt, message.committableOffset))
      .groupWithin(3, FiniteDuration(5, TimeUnit.SECONDS)) // emit each 3 elements or each 5 seconds
      .map(r => {
      val list: Seq[(Int, CommittableOffset[IO])] = r.toList
      println(list)
      list.last._2 // last offset of current chunk
    })
      .through(commitBatch)

    stream.compile.drain.as(ExitCode.Success)
  }
}

object Fs2KafkaProducer extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val producerSettings =
      ProducerSettings[String, String]
        .withBootstrapServers("localhost:9092")

    val stream =
      producerStream[IO]
        .using(producerSettings)
        .flatMap { producer =>
          Stream
            .fixedDelay(FiniteDuration(1, TimeUnit.SECONDS))
            .repeat
            .map { _ =>
              val value = s"${Random.nextInt(10)}"
              println(value)
              val record = ProducerRecord("source", "", value)
              ProducerMessage.one(record)
            }
            .evalMap(producer.producePassthrough)
        }

    stream.compile.drain.as(ExitCode.Success)
  }
}