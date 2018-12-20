package akka.nifi.stream.postgres.service

import akka.nifi.stream.postgres.repository.SQLRepository
import scalikejdbc._

import scala.util.Try

trait SQLService[Entity, PK] extends DefaultService[Entity, PK] {
  override type A = SQLRepository[Entity, PK]

  override def findOne(pk: PK): Either[Throwable, Option[Entity]] = tx(s => repository.findOne(pk).run(s).value.unsafeRunSync())

  override def findAll(): Either[Throwable, Seq[Entity]] = tx(s => repository.findAll().run(s).unsafeRunSync())

  override def delete(pk: PK): Either[Throwable, Unit] = tx(s => repository.delete(pk).run(s).unsafeRunSync())

  override def save(e: Entity): Either[Throwable, Entity] = tx(s => repository.save(e).run(s).unsafeRunSync())

  override def update(e: Entity): Either[Throwable, Entity] = tx(s => repository.update(e).run(s).unsafeRunSync())

  final def tx[A](f: DBSession => A): Either[Throwable, A] = Try {
    DB localTx {
      implicit session => {
        f(session)
      }
    }
  }.toEither
}
