package akka.mongo.crud.repository

import cats.effect.IO

import scala.reflect.ClassTag

trait Repository[A, ID] {
  def insert(a: A)(implicit classTag: ClassTag[A]): IO[_]

  def delete(id: ID)(implicit classTag: ClassTag[A]): IO[_]

  def update(a: A)(implicit classTag: ClassTag[A]): IO[_]

  def find(id: ID)(implicit classTag: ClassTag[A]): IO[_]

  def findAll()(implicit classTag: ClassTag[A]): IO[_]
}
