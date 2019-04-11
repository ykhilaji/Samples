import java.io.File

import org.scalameter.api._

object SimpleTimeMeasurement extends Bench.LocalTime {
  val sizes = Gen.range("size")(300000, 1500000, 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  performance of "Range" in {
    measure method "map" in {
      using(ranges) in {
        r => r.map(_ + 1)
      }
    }
  }
}

object SimpleMemoryMeasurement extends Bench.OfflineReport {
  override lazy val persistor = SerializationPersistor(new File("target/scalameter/performance/results"))

  override def measurer: Measurer[Double] = new Executor.Measurer.MemoryFootprint

  val sizes = Gen.range("size")(300000, 1500000, 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  performance of "Range" in {
    measure method "map" in {
      using(ranges) in {
        r => r.map(_ + 1)
      }
    }
  }
}
