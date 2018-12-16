package akka.infinispan.crud.repository

import cats.effect._
import javax.transaction.TransactionManager

trait Repository[ID, A] {
  def findOne(id: ID): IO[Option[A]]

  def findAll(): IO[Seq[A]]

  def delete(id: ID): IO[Unit]

  def deleteAll(): IO[Unit]

  def save(a: A): IO[Unit]

  def update(a: A): IO[Unit]

  def transaction: TransactionManager
}
