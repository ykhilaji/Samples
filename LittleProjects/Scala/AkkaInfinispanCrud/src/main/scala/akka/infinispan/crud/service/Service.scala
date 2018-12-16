package akka.infinispan.crud.service

import akka.infinispan.crud.repository.Repository
import cats.effect._
import cats.implicits._
import org.apache.logging.log4j.Logger

trait Service[ID, A] {
  val logger: Logger
  val repository: Repository[ID, A]

  def findOne(id: ID): Either[Throwable, Option[A]] = {
    logger.info(s"Trying to find entity by id: $id")
    execute(repository.findOne(id))
  }

  def findAll(): Either[Throwable, Seq[A]] = {
    logger.info("Trying to find all entities")
    execute(repository.findAll())
  }

  def delete(id: ID): Either[Throwable, Unit] = {
    logger.info(s"Trying to delete entity by id: $id")
    execute(repository.delete(id))
  }

  def deleteAll(): Either[Throwable, Unit] = {
    logger.info("Trying to delete all entities")
    execute(repository.deleteAll())
  }

  def save(a: A): Either[Throwable, Unit] = {
    logger.info(s"Trying to save entity: $a")
    execute(repository.save(a))
  }

  def saveAll(a: Seq[A]): Either[Throwable, Seq[Unit]] = execute(a.map(e => repository.save(e)).toList.traverse[IO, Unit](identity))

  def update(a: A): Either[Throwable, Unit] = {
    logger.info(s"Trying to update entity: $a")
    execute(repository.update(a))
  }

  private def execute[B](f: => IO[B]): Either[Throwable, B] = {
    val tx = repository.transaction
    (for {
      _ <- IO(tx.begin())
      r <- f
      _ <- IO(tx.commit())
    } yield r).attempt.guaranteeCase({
      case cats.effect.ExitCase.Error(e) =>
        for {
          _ <- IO(logger.error(s"Error: ${e.getLocalizedMessage}. Trying to rollback transaction"))
          _ <- IO(tx.rollback())
          _ <- IO(logger.error("Successfully rollback transaction"))
        } yield ()
      case _ =>
        for {
          _ <- IO(logger.info("Successful execution"))
        } yield ()
    }).unsafeRunSync()
  }
}
