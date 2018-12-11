package akka.mongo.crud.model

import org.mongodb.scala.bson.ObjectId

case class Event(_id: ObjectId = new ObjectId(), `type`: String, description: String, eventSource: ObjectId) extends MongoModel

object Event {
  def apply(`type`: String, description: String, eventSource: ObjectId): Event =
    new Event(new ObjectId(), `type`, description, eventSource)
}