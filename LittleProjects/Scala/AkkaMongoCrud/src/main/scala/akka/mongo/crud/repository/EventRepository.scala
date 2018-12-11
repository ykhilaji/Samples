package akka.mongo.crud.repository

import akka.mongo.crud.configuration.DataSource
import akka.mongo.crud.model.{CountByTypeAggregation, Event}
import cats.effect.IO
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.result.DeleteResult

import scala.concurrent.Future


class EventRepository extends MongoRepository[Event, ObjectId] {
  override val collection: MongoCollection[Event] = DataSource.db.getCollection("event")

  def findByType(`type`: String): IO[Future[Seq[Event]]] =
    IO(collection.find(equal("type", `type`)).sort(ascending("_id")).toFuture())

  def findByTypeRegex(pattern: String): IO[Future[Seq[Event]]] =
    IO(collection.find(regex("type", pattern)).sort(ascending("_id")).toFuture())

  def findByEventSource(_id: ObjectId): IO[Future[Seq[Event]]] =
    IO(collection.find(equal("eventSource", _id)).sort(ascending("_id")).toFuture())

  def countByType(`type`: String): IO[Future[Option[CountByTypeAggregation]]] =
    IO(collection
      .aggregate[CountByTypeAggregation](Seq(
      filter(equal("type", `type`)),
        group("$type",
          sum("total", 1)),
        out("countByType")))
        .headOption())

  def deleteByEventSource(_id: ObjectId): IO[Future[DeleteResult]] = deleteMany(equal("eventSource", _id))
}

object EventRepository {
  def apply(): EventRepository = new EventRepository()
}