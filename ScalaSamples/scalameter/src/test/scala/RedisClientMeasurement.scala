import org.scalameter.api._
import com.redis._

//10000 records > 20 seconds [???]
object RedisClientMeasurement extends Bench.LocalTime {
  var client: RedisClientPool = _
  val gen = Gen.exponential("records")(100, 10000, 100)

  performance of "redis client" in {
    measure method "set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        client = new RedisClientPool("192.168.99.100", 6379)
      } tearDown { _ =>
        client.withClient(c => c.flushall)
        client.close
      } in { el =>
        (0 to el).foreach(i => client.withClient(client => client.set(i, i)))
      }
    }

    measure method "parallel set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        client = new RedisClientPool("192.168.99.100", 6379)
      } tearDown { _ =>
        client.withClient(c => c.flushall)
        client.close
      } in { el =>
        (0 to el).par.foreach(i => client.withClient(client => client.set(i, i)))
      }
    }

    measure method "batch set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        client = new RedisClientPool("192.168.99.100", 6379)
      } tearDown { _ =>
        client.withClient(c => c.flushall)
        client.close
      } in { el =>
        (0 to 10).foreach(_ => (0 to el / 10).foreach(i => client.withClient(c => c.pipeline(p => p.set(i, i)))))
      }
    }

    measure method "parallel batch set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        client = new RedisClientPool("192.168.99.100", 6379)
      } tearDown { _ =>
        client.withClient(c => c.flushall)
        client.close
      } in { el =>
        (0 to 10).par.foreach(_ => (0 to el / 10).foreach(i => client.withClient(c => c.pipeline(p => p.set(i, i)))))
      }
    }
  }
}
