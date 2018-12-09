package akka.mongo.crud.model

import org.mongodb.scala.bson.ObjectId

trait MongoModel {
  val _id: ObjectId
}
