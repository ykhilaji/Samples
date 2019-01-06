package akka.quill.sql.crud.repository

import cats.data.OptionT
import cats.effect.IO

trait Repository[A, ID] {
  def findOne(id: ID): OptionT[IO, A]

  def findAll(): IO[Seq[A]]

  def save(a: A): IO[_]

  def update(a: A): IO[_]

  def deleteOne(id: ID): IO[_]

  def deleteAll(): IO[_]
}
