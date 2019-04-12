import akka.actor.ActorSystem
import org.scalameter.api._

import scala.concurrent.duration._
import scredis._

import scala.concurrent.{Await, Future}

// Parameters(records -> 100000): 149.133367 ms
object ScRedisMeasurement extends Bench.LocalTime {
  var system: ActorSystem = _
  var client: Client = _
  val gen = Gen.exponential("records")(100, 1000000, 100)

  performance of "redis client" in {
    measure method "set" in {
      using(gen) config {
        exec.maxWarmupRuns -> 1
        exec.benchRuns -> 1
        exec.independentSamples -> 1
      } setUp { _ =>
        system = ActorSystem()
        client = Client("192.168.99.100", 6379)(system)
      } tearDown { _ =>
        Await.ready(client.flushAll(), 5 seconds)
        Await.result(client.quit(), 5 seconds)
        Await.ready(system.terminate(), 5 seconds)
      } in { el =>
        implicit val ec = system.dispatcher
        val future = Future.traverse(1 to el) { i =>
          client.set(s"$i", i)
        }
        Await.result(future, 30 seconds)
      }
    }
  }
}
