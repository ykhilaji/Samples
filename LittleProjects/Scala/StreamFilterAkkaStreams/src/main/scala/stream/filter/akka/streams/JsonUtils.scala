package stream.filter.akka.streams

import spray.json._
import DefaultJsonProtocol._

private object JsonProtocol extends DefaultJsonProtocol {
  implicit val entityFormat: RootJsonFormat[Entity] = jsonFormat1(Entity)
}

object JsonUtils {
  import JsonProtocol._

  def parse(json: String): Entity = json.parseJson.convertTo[Entity]

  def toJson(entity: Entity): String = entity.toJson.toString()
}
