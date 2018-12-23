package akka.nifi.stream.postgres.service

import akka.nifi.stream.postgres.repository.SQLRepository
import scalikejdbc._

import scala.util.Try

trait SQLService[Entity, PK] extends DefaultService[Entity, PK] {
  override type A = SQLRepository[Entity, PK]

  override def findOne(pk: PK): Either[Throwable, Option[Entity]] = tx(implicit s => repository.findOne(pk).value.unsafeRunSync())

  override def findAll(): Either[Throwable, Seq[Entity]] = tx(implicit s => repository.findAll().unsafeRunSync())

  override def delete(pk: PK): Either[Throwable, Unit] = tx(implicit s => repository.delete(pk).unsafeRunSync())

  override def save(e: Entity): Either[Throwable, Entity] = tx(implicit s => repository.save(e).unsafeRunSync())

  override def update(e: Entity): Either[Throwable, Entity] = tx(implicit s => repository.update(e).unsafeRunSync())

  final def tx[A](f: DBSession => A): Either[Throwable, A] = Try {
    DB localTx {
      implicit session => {
        f(session)
      }
    }
  }.toEither
}
