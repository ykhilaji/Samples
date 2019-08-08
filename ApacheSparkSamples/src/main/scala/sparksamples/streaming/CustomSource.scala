package sparksamples.streaming

import java.util.concurrent.{Executors, ThreadLocalRandom, TimeUnit}

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{StreamingContext, Time}
import org.apache.spark.streaming.dstream.{InputDStream, ReceiverInputDStream}
import org.apache.spark.streaming.receiver.Receiver

// Generate random ints
// Not reliable source
class CustomSource(delay: Long) extends Receiver[Int](StorageLevel.MEMORY_ONLY) {
  if (delay <= 0) {
    throw new IllegalArgumentException("Delay should be > 0")
  }

  @transient
  lazy val executor = Executors.newScheduledThreadPool(1)

  override def onStart(): Unit = {
    executor.scheduleAtFixedRate(new Runnable {
      override def run(): Unit =
        if (!isStopped()) {
          store(ThreadLocalRandom.current().nextInt())
        } else {
          stop("Stop receiver")
        }
    }, 0, delay, TimeUnit.MILLISECONDS)
  }

  override def onStop(): Unit = executor.shutdown()
}

class CustomSourceUsingInputDStream(
                                     ssc: StreamingContext,
                                     params: Map[String, String]) extends ReceiverInputDStream[Int](ssc) {
  override def getReceiver(): Receiver[Int] = new CustomSource(params.get("delay").map(_.toLong).getOrElse(100L))
}