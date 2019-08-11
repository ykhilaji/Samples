package stream.filter.akka.streams

import org.scalatest.{Matchers, WordSpec}

class JsonUtilsTest extends WordSpec with Matchers {
  "JsonUtils" should {
    "parse json to entity" in {
      val json = """ {"key": "1"} """
      assertResult(Entity("1"))(JsonUtils.parse(json))
    }

    "convert entity to json" in {
      val entity = Entity("1")
      assertResult("""{"key":"1"}""")(JsonUtils.toJson(entity))
    }
  }
}
