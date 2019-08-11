package stream.filter.akka.streams

import akka.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import akka.kafka.ConsumerMessage
import akka.stream.Attributes


object Logic {
  def filter(cache: Cache) =
    Flow[Message[_, Entity]]
      .via(log("before-filter"))
      .filter(msg => cache.isExist(msg.value))
      .via(log("after-filter"))

  def filterAsync(cache: Cache, parallelism: Int)(implicit ex: ExecutionContext) =
    Flow[Message[_, Entity]]
      .via(log("before-async-filter"))
      .mapAsync(parallelism)(e => cache.isExistAsync(e.value).map(f => (e, f)))
      .filter(_._2)
      .map(_._1)
      .via(log("after-async-filter"))

  /**
    * Chunk up this stream into groups of elements received within a time window,
    * or limited by the given number of elements, whatever happens first.
    */
  def filterBatch(cache: Cache, batchSize: Int, window: FiniteDuration) =
    Flow[Message[_, Entity]]
      .via(log("before-batch-filter"))
      .groupedWithin(batchSize, window)
      .via(log("batch-filter-group"))
      .map(msg => cache.isExist(msg.map(_.value)).zip(msg))
      .mapConcat(identity)
      .filter(_._1)
      .map(_._2)
      .via(log("after-batch-filter"))

  def filterBatchAsync(cache: Cache, batchSize: Int, window: FiniteDuration, parallelism: Int)(implicit ex: ExecutionContext) =
    Flow[Message[_, Entity]]
      .via(log("before-batch-async-filter"))
      .groupedWithin(batchSize, window)
      .via(log("batch-async-filter-group"))
      .mapAsync(parallelism)(e => cache.isExistAsync(e.map(_.value)).map(_.zip(e)))
      .mapConcat(identity)
      .filter(_._1)
      .map(_._2)
      .via(log("after-batch-async-filter"))

  def toEntity() = {
    Flow[ConsumerMessage.CommittableMessage[String, String]].map { msg =>
      CommitableMessage[String, Entity](msg.record.key(), JsonUtils.parse(msg.record.value()), msg.committableOffset)
    }
  }

  def toJsonString() = {
    Flow[CommitableMessage[String, Entity]].map { msg =>
      CommitableMessage[String, String](msg.key, JsonUtils.toJson(msg.value), msg.committableOffset)
    }
  }

  private def log[A](name: String) = {
    Flow[A].log(name).addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Error,
        onFinish = Attributes.LogLevels.Info))
  }
}
