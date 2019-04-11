import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, TimeUnit}

import org.scalameter.api._
import scala.concurrent.stm._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}

object PerformanceTest extends Bench.OfflineReport {
  override lazy val persistor = SerializationPersistor(new File("target/scalameter/threading/results"))

  val threads = Gen.range("threads")(1, 32, 1)

  performance of "parallel processing" in {
    measure method "synchronized" in {
      using(threads) in { t =>
        implicit val executionContext: ExecutionContextExecutorService =
          ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(t))

        var a = 0
        def inc(): Unit = this.synchronized {
          a += 1
        }

        Await.ready(Future.sequence((1 to 1000).map(_ => Future(inc()))), Duration(15, TimeUnit.SECONDS))
        executionContext.shutdown()
      }
    }

    measure method "atomic var" in {
      using(threads) in { t =>
        implicit val executionContext: ExecutionContextExecutorService =
          ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(t))

        val a = new AtomicInteger(0)

        Await.ready(Future.sequence((1 to 1000).map(_ => Future(a.incrementAndGet()))), Duration(15, TimeUnit.SECONDS))
        executionContext.shutdown()
      }
    }

    measure method "atomic block" in {
      using(threads) in { t =>
        implicit val executionContext: ExecutionContextExecutorService =
          ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(t))

        val a = Ref[Int](0)

        Await.ready(Future.sequence((1 to 1000).map(_ => Future({
          atomic { implicit tx =>
            a() = a() + 1
          }
        }))), Duration(15, TimeUnit.SECONDS))
        executionContext.shutdown()
      }
    }

    measure method "single transform" in {
      using(threads) in { t =>
        implicit val executionContext: ExecutionContextExecutorService =
          ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(t))

        val a = Ref[Int](0)

        Await.ready(Future.sequence((1 to 1000).map(_ => Future({
          a.single.transform(_ + 1)
        }))), Duration(15, TimeUnit.SECONDS))
        executionContext.shutdown()
      }
    }
  }
}
