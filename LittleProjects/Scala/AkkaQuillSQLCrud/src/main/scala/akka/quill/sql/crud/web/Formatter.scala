package akka.quill.sql.crud.web

import akka.quill.sql.crud.model.Entity
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object Formatter {
  implicit val entityDecoder: Decoder[Entity] = Decoder.forProduct2[Entity, Long, String]("id", "value")((id, value) => Entity(id, value))
  implicit val entityEncoder: Encoder[Entity] = deriveEncoder[Entity]
}
