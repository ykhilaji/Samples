package akka.infinispan.crud.web

import akka.infinispan.crud.model.Entity
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormatter extends DefaultJsonProtocol {
  implicit val entityToJson: RootJsonFormat[Entity] = jsonFormat2(Entity.apply)
}
