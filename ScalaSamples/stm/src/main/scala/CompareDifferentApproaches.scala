import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.stm._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object Util {
  implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

  def duration(block: => Unit): Long = {
    val start = System.nanoTime()
    block
    (System.nanoTime() - start) / 1000000
  }
}

import Util._

object FutureAtomicValueTest {
  def main(args: Array[String]): Unit = {
    var total: Double = 0
    var min: Double = Double.MaxValue
    var max: Double = Double.MinValue
    for(_ <- 1 to 1000) {
      val a = new AtomicInteger(0)

      val tm = duration(Await.ready(Future.sequence((1 to 1000).map(_ => Future(a.incrementAndGet()))), Duration(5, TimeUnit.SECONDS)))
      total += tm // ms
      min = Math.min(min, tm)
      max = Math.max(max, tm)

      if (a.get() != 1000) {
        throw new Exception("A is not equal 1000")
      }
    }

    println(s"Average time: ${total / 1000}")
    println(s"Min time: $min")
    println(s"max time: $max")

    executionContext.shutdown()
  }
}

object FutureSyncTest {
  def main(args: Array[String]): Unit = {
    var total: Double = 0
    var min: Double = Double.MaxValue
    var max: Double = Double.MinValue

    for(_ <- 1 to 1000) {
      var a = 0
      def inc(): Unit = this.synchronized {
        a += 1
      }

      val tm = duration(Await.result(Future.sequence((1 to 1000).map(_ => Future(inc()))), Duration(5, TimeUnit.SECONDS)))
      total += tm // ms
      min = Math.min(min, tm)
      max = Math.max(max, tm)
      if (a != 1000) {
        throw new Exception("A is not equal 1000")
      }
    }

    println(s"Average time: ${total / 1000}")
    println(s"Min time: $min")
    println(s"max time: $max")

    executionContext.shutdown()
  }
}

object FutureAtomicBlockTest {
  def main(args: Array[String]): Unit = {
    var total: Double = 0
    var min: Double = Double.MaxValue
    var max: Double = Double.MinValue

    for(_ <- 1 to 1000) {
      val a = Ref[Int](0)
      def inc(): Unit = atomic { implicit tx =>
        a() = a() + 1
      }

      val tm = duration(Await.result(Future.sequence((1 to 1000).map(_ => Future(inc()))), Duration(5, TimeUnit.SECONDS)))
      total += tm // ms
      min = Math.min(min, tm)
      max = Math.max(max, tm)
      if (a.single.get != 1000) {
        throw new Exception("A is not equal 1000")
      }
    }

    println(s"Average time: ${total / 1000}")
    println(s"Min time: $min")
    println(s"max time: $max")

    executionContext.shutdown()
  }
}

object FutureAtomicTransactionTransformTest {
  def main(args: Array[String]): Unit = {
    var total: Double = 0
    var min: Double = Double.MaxValue
    var max: Double = Double.MinValue

    for(_ <- 1 to 1000) {
      val a = Ref[Int](0)
      def inc(): Unit = a.single.transform(_ + 1)

      val tm = duration(Await.result(Future.sequence((1 to 1000).map(_ => Future(inc()))), Duration(5, TimeUnit.SECONDS)))
      total += tm // ms
      min = Math.min(min, tm)
      max = Math.max(max, tm)
      if (a.single.get != 1000) {
        throw new Exception("A is not equal 1000")
      }
    }

    println(s"Average time: ${total / 1000}")
    println(s"Min time: $min")
    println(s"max time: $max")

    executionContext.shutdown()
  }
}

object FutureAtomicTransactionSimpleIncrementTest {
  def main(args: Array[String]): Unit = {
    var total: Double = 0
    var min: Double = Double.MaxValue
    var max: Double = Double.MinValue

    for(_ <- 1 to 1000) {
      val a = Ref[Int](0)
      def inc(): Unit = a.single.+=(1)

      val tm = duration(Await.result(Future.sequence((1 to 1000).map(_ => Future(inc()))), Duration(5, TimeUnit.SECONDS)))
      total += tm // ms
      min = Math.min(min, tm)
      max = Math.max(max, tm)
      if (a.single.get != 1000) {
        throw new Exception("A is not equal 1000")
      }
    }

    println(s"Average time: ${total / 1000}")
    println(s"Min time: $min")
    println(s"max time: $max")

    executionContext.shutdown()
  }
}