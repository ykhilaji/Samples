package memcached

import org.scalameter.api._
import org.scalameter.api.{Bench, Gen}
import shade.memcached.{Configuration, Memcached}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * cores: 4
  * name: Java HotSpot(TM) 64-Bit Server VM
  * osArch: amd64
  * osName: Windows 7
  * vendor: Oracle Corporation
  * version: 25.131-b11
  */
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
