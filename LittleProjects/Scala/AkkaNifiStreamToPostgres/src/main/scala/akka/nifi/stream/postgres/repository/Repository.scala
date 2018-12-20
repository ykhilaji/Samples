package akka.nifi.stream.postgres.repository

import cats.effect.IO
import cats.data.{OptionT, Reader}

trait Repository[Entity, PK] {
  type Context

  def findOne(pk: PK): Reader[Context, OptionT[IO, Entity]]

  def findAll(): Reader[Context, IO[Seq[Entity]]]

  def delete(pk: PK): Reader[Context, IO[Unit]]

  def save(e: Entity): Reader[Context, IO[Entity]]

  def update(e: Entity): Reader[Context, IO[Entity]]
}

object Repository {
  type Aux[E, P, C] = Repository[E, P] {type Context = C}
}