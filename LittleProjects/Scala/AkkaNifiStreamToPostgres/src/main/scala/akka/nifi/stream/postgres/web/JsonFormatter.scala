package akka.nifi.stream.postgres.web

import akka.nifi.stream.postgres.model.{Entity, Event, UnknownEvent}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object JsonFormatter {
  implicit val entityDecoder: Decoder[Entity] = deriveDecoder[Entity]
  implicit val entityEncoder: Encoder[Entity] = deriveEncoder[Entity]

  implicit val eventDecoder: Decoder[Event] = deriveDecoder[Event]
  implicit val eventEncoder: Encoder[Event] = deriveEncoder[Event]

  implicit val unknownEventDecoder: Decoder[UnknownEvent] = deriveDecoder[UnknownEvent]
  implicit val unknownEventEncoder: Encoder[UnknownEvent] = deriveEncoder[UnknownEvent]
}
