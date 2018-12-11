package akka.mongo.crud.repository

import akka.mongo.crud.configuration.DataSource
import akka.mongo.crud.model.EventSource
import cats.effect.IO
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.result.DeleteResult
//import com.mongodb.client.model.IndexOptions
//import org.mongodb.scala.model.Indexes._

import scala.concurrent.Future

class EventSourceRepository extends MongoRepository[EventSource, ObjectId] {
  override val collection: MongoCollection[EventSource] = DataSource.db.getCollection("source")
//  collection.createIndex(ascending("name"), new IndexOptions().unique(true))

  def findByName(name: String): IO[Future[Option[EventSource]]] = findOne(equal("name", name))

  def findByNameRegex(pattern: String): IO[Future[Seq[EventSource]]] = findMany(regex("name", pattern))

  def deleteByName(name: String): IO[Future[DeleteResult]] = deleteOne(equal("name", name))

  def deleteByNameRegex(pattern: String): IO[Future[DeleteResult]] = deleteMany(equal("name", pattern))

  def findAllSortByNameAsc(): IO[Future[Seq[EventSource]]] = IO(collection.find().sort(ascending("name")).toFuture())

  def findAllSortByNameDesc(): IO[Future[Seq[EventSource]]] = IO(collection.find().sort(descending("name")).toFuture())

  def count(): IO[Future[Long]] = IO(collection.countDocuments().toFuture())
}

object EventSourceRepository {
  def apply(): EventSourceRepository = new EventSourceRepository()
}