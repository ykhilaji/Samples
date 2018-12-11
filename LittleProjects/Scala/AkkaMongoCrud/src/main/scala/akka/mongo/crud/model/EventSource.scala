package akka.mongo.crud.model

import org.mongodb.scala.bson.ObjectId

case class EventSource(_id: ObjectId = new ObjectId(), description: String, name: String) extends MongoModel

object EventSource {
  def apply(description: String, name: String): EventSource = new EventSource(new ObjectId(), description, name)
}