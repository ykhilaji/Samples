package akka.nifi.stream.postgres.repository

import cats.effect.IO
import cats.data.OptionT

trait Repository[Entity, PK] {
  type Context

  def findOne(pk: PK)(implicit context: Context):OptionT[IO, Entity]

  def findAll()(implicit context: Context): IO[Seq[Entity]]

  def delete(pk: PK)(implicit context: Context): IO[Unit]

  def save(e: Entity)(implicit context: Context): IO[Entity]

  def update(e: Entity)(implicit context: Context): IO[Entity]
}

object Repository {
  type Aux[E, P, C] = Repository[E, P] {type Context = C}
}