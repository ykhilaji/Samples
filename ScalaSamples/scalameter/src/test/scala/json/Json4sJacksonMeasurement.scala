package json

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.scalameter.api._
import org.scalameter.picklers.Implicits._

/**
  * cores: 4
  * name: Java HotSpot(TM) 64-Bit Server VM
  * osArch: amd64
  * osName: Windows 7
  * vendor: Oracle Corporation
  * version: 25.131-b11
  */
object Json4sJacksonMeasurement extends Bench.LocalTime {
  // 100k instead of 1kk
  val records = Gen.single("records")(100000)

  case class A(f1: Long, f2: Double, f3: String, f4: Array[Long], f5: Map[String, Int])

  object Formats {
    implicit val noTypeHintsFormat = Serialization.formats(NoTypeHints)
    implicit val defaultFormat: DefaultFormats.type = DefaultFormats
  }

  performance of "json4s" in {
    // Parameters(records -> 100000): 69.295905 ms
    measure method "string parse" in {
      using(records) in { el =>
        (0 to el).foreach(i => {
          val r: JValue = parse(s"""{"value": $i}""")
        })
      }
    }

    // Parameters(records -> 100000): 1035.643826 ms
    measure method "object to json" in {
      using(records) in { el =>
        import Formats.noTypeHintsFormat
        (0 to el).foreach(i => write(A(i, i / 2.0, i.toString, Array(i), Map(i.toString -> i))))
      }
    }

    // Parameters(records -> 100000): 1971.687709 ms
    measure method "extract object from json" in {
      using(records) in { el =>
        import Formats.defaultFormat
        (0 to el).foreach(i => parse(s"""{"f1": $i, "f2": ${i / 2}, "f3": "$i", "f4": [$i], "f5": {"$i": $i} }""").extract[A])
      }
    }

    // Parameters(records -> 100000): 2159.414376 ms
    measure method "deserialize json to object" in {
      using(records) in { el =>
        import Formats.noTypeHintsFormat
        (0 to el).foreach(i => read[A](s"""{"f1": $i, "f2": ${i / 2}, "f3": "$i", "f4": [$i], "f5": {"$i": $i} }"""))
      }
    }
  }
}
