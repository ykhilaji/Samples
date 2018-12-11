package akka.mongo.crud.web

import akka.mongo.crud.model.{CountByTypeAggregation, Event, EventSource}
import org.mongodb.scala.bson.{BsonObjectId, ObjectId}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

case class EventSourceRequest(description: String, name: String)

case class EventRequest(`type`: String, description: String, _id: ObjectId)

object JsonFormats extends DefaultJsonProtocol {
  implicit object objectIdToJson extends RootJsonFormat[ObjectId] {
    override def read(json: JsValue): ObjectId = json match {
      case spray.json.JsString(value) => BsonObjectId.apply(value).getValue
    }

    override def write(obj: ObjectId): JsValue = JsString(obj.toHexString)
  }

  implicit val eventToJson: RootJsonFormat[Event] = jsonFormat4(Event.apply)
  implicit val eventRequestToJson: RootJsonFormat[EventRequest] = jsonFormat3(EventRequest.apply)
  implicit val eventSourceToJson: RootJsonFormat[EventSource] = jsonFormat3(EventSource.apply)
  implicit val eventSourceRequestToJson: RootJsonFormat[EventSourceRequest] = jsonFormat2(EventSourceRequest.apply)
  implicit val countByTypeAggregationToJson: RootJsonFormat[CountByTypeAggregation] = jsonFormat2(CountByTypeAggregation.apply)
}
