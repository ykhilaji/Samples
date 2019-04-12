import org.scalameter.api._
import shade.memcached._

import scala.concurrent.ExecutionContext.Implicits.global
import concurrent.duration._
import concurrent.{Await, Future}
import scala.collection.immutable

object ShadeMemcachedMeasurement extends Bench.LocalTime {
  var memcached: Memcached = _
  val gen = Gen.exponential("records")(100, 10000, 100)

  // Parameters(records -> 10000): 1578.919773 ms
  performance of "memcached shade client" in {
    measure method "blocking set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        memcached = Memcached(Configuration("192.168.99.100:11211"))
      } tearDown { _ =>
        memcached.close()
      } in { el =>
        (0 to el).foreach(i => memcached.awaitSet(s"$i", i, 10 seconds))
      }
    }

    // Parameters(records -> 10000): 540.041418 ms
    measure method "non-blocking set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        memcached = Memcached(Configuration("192.168.99.100:11211"))
      } tearDown { _ =>
        memcached.close()
      } in { el =>
        val f: Future[immutable.IndexedSeq[Unit]] = Future.sequence((0 to el).map(i => {
          val m: Future[Unit] = memcached.set(s"$i", i, 10 seconds)
          m
        }))
        Await.ready(f, Duration.Inf)
      }
    }
  }
}
