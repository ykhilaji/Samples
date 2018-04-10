package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat, RootJsonFormat}

object JsonWrapper extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val entity2json: RootJsonFormat[Entity] = jsonFormat2(Entity.apply)
  implicit val response2json: RootJsonFormat[Response] = jsonFormat2(Response)

  implicit def mono2json[A: JsonFormat] = jsonFormat1(Mono.apply[A])

  implicit def flux2json[A: JsonFormat] = jsonFormat1(Flux.apply[A])
}
