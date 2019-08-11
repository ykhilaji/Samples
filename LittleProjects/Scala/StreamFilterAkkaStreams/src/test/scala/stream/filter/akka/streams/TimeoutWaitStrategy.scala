package stream.filter.akka.streams

import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy

import scala.concurrent.duration.FiniteDuration

class TimeoutWaitStrategy(duration: FiniteDuration) extends AbstractWaitStrategy {
  override def waitUntilReady(): Unit = Thread.sleep(duration.toMillis)
}

object TimeoutWaitStrategy {
  def apply(duration: FiniteDuration): TimeoutWaitStrategy = new TimeoutWaitStrategy(duration)
}