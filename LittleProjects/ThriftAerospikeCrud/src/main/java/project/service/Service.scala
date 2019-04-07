package project.service

import scala.concurrent.Future

trait Service[F[_], A, ID] {
  def findOne(id: ID): F[Option[A]]

  def findAll(): F[Seq[A]]

  def create(a: A): F[A]

  def update(a: A): F[Unit]

  def remove(id: ID): F[Boolean]

  def removeAll(): F[Unit]
}
