package akka.mongo.crud.repository

import akka.mongo.crud.model.MongoModel
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.{Completed, FindObservable, MongoCollection, SingleObservable}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import cats.effect.IO

import scala.concurrent.Future
import scala.reflect.ClassTag

trait MongoRepository[A <: MongoModel, ID] extends Repository[A, ObjectId] {
  val collection: MongoCollection[A]

  def findMany(filter: Bson)(implicit classTag: ClassTag[A]): IO[Future[Seq[A]]] = IO(collection.find[A](filter).toFuture())

  def findOne(filter: Bson)(implicit classTag: ClassTag[A]): IO[Future[Option[A]]] = IO(collection.find[A](filter).headOption())

  def deleteOne(filter: Bson): IO[Future[DeleteResult]] = IO(collection.deleteOne(filter).head())

  def deleteMany(filter: Bson): IO[Future[DeleteResult]] = IO(collection.deleteMany(filter).head())

  def updateOne(filter: Bson, update: Bson): IO[Future[UpdateResult]] = IO(collection.updateOne(filter, update).head())

  def updateMany(filter: Bson, update: Bson): IO[Future[UpdateResult]] = IO(collection.updateMany(filter, update).head())

  override def insert(a: A)(implicit classTag: ClassTag[A]): IO[Future[Completed]] = IO(collection.insertOne(a).head())

  override def delete(id: ObjectId)(implicit classTag: ClassTag[A]): IO[Future[DeleteResult]] = deleteOne(equal("_id", id))

  override def update(a: A)(implicit classTag: ClassTag[A]): IO[Future[Completed]] =  for {
    _ <- deleteOne(equal("_id", a._id))
    r <- insert(a)
  } yield r

  override def find(id: ObjectId)(implicit classTag: ClassTag[A]): IO[Future[Option[A]]] = findOne(equal("_id", id))

  override def findAll()(implicit classTag: ClassTag[A]): IO[Future[Seq[A]]] = IO(collection.find().toFuture())
}
