package json

import org.scalameter.api._
import org.scalameter.picklers.Implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

/**
  * cores: 4
  * name: Java HotSpot(TM) 64-Bit Server VM
  * osArch: amd64
  * osName: Windows 7
  * vendor: Oracle Corporation
  * version: 25.131-b11
  */
object CirceMeasurement extends Bench.LocalTime {
  val records = Gen.single("records")(1000000)

  case class A(f1: Long, f2: Double, f3: String, f4: Array[Long], f5: Map[String, Int])

  performance of "json4s" in {
    // Parameters(records -> 1000000): 404.676632 ms
    measure method "string parse" in {
      using(records) in { el =>
        (0 to el).foreach(i => {
          val r: Either[ParsingFailure, Json] = parse(s"""{"value": $i}""")
        })
      }
    }

    // Parameters(records -> 1000000): 1527.557745 ms
    measure method "object to json" in {
      using(records) in { el =>
        (0 to el).foreach(i => A(i, i / 2.0, i.toString, Array(i), Map(i.toString -> i)).asJson)
      }
    }

    // Parameters(records -> 1000000): 840.067913 ms
    measure method "json to object" in {
      using(records) in { el =>
        (0 to el).foreach(i => s"""{"f1": $i, "f2": ${i / 2}, "f3": "$i", "f4": [$i], "f5": {"$i": $i} }""".asJson.as[A])
      }
    }
  }
}
