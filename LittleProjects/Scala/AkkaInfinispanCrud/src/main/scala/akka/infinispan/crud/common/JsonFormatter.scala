package akka.infinispan.crud.common

import akka.infinispan.crud.model.Entity
import spray.json.DefaultJsonProtocol

object JsonFormatter extends DefaultJsonProtocol {
  implicit val entityToJson = jsonFormat2(Entity.apply)
}
