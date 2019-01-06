package akka.quill.sql.crud.repository

import akka.quill.sql.crud.model.Entity
import cats.data.OptionT
import cats.effect.IO
import akka.quill.sql.crud.configuration.DataSource.ctx
import akka.quill.sql.crud.configuration.DataSource.ctx._
import org.apache.logging.log4j.{LogManager, Logger}

class EntityRepository extends Repository[Entity, Long] {
  val logger: Logger = LogManager.getLogger("entity-repository")

  // see schema name configuration in application.conf
  implicit val entityMeta = schemaMeta[Entity]("entity", _.createTime -> "create_time")
  // exclude columns from insert/update operations
  implicit val entityInsertMeta = insertMeta[Entity](_.id, _.createTime)
  implicit val entityUpdateMeta = updateMeta[Entity](_.id, _.createTime)

  override def findOne(id: Long): OptionT[IO, Entity] = for {
    _ <- OptionT.liftF(IO(logger.info(s"Trying to find entity with id: $id")))
    entity <- OptionT(IO(ctx.run(query[Entity].filter(_.id == lift(id))).headOption))
    _ <- OptionT.liftF(IO(logger.info(s"Founded entity: $entity")))
  } yield entity

  override def findAll(): IO[Seq[Entity]] = for {
    _ <- IO(logger.info("Trying to find all entities"))
    e <- IO(ctx.run(query[Entity]))
    _ <- IO(logger.info("All entities were founded"))
  } yield e

  override def save(a: Entity): IO[Entity] = for {
    _ <- IO(logger.info(s"Trying to save entity: $a"))
    id <- IO(ctx.run(query[Entity].insert(lift(a)).returning[Long](_.id)))
    _ <- IO(logger.info(s"Entity: ${a.copy(id = id)} was saved")) // redundant copy overhead
  } yield a.copy(id = id)

  override def update(a: Entity): IO[Entity] = for {
    e <- findOne(a.id).getOrElse(throw new Exception(s"No such entity with id: ${a.id}"))
    _ <- IO(ctx.run(query[Entity].filter(_.id == lift(a.id)).update(_.value -> lift(a.value))))
    _ <- IO(logger.info("Entity was updated"))
  } yield e.copy(value = a.value)

  override def deleteOne(id: Long): IO[Long] = for {
    _ <- IO(logger.info(s"Delete entity with id: $id"))
    e <- IO(ctx.run(query[Entity].filter(_.id == lift(id)).delete))
    _ <- IO(logger.info(s"Deleted entities count: $e"))
  } yield e

  override def deleteAll(): IO[Long] = for {
    _ <- IO(logger.info("Deleting all entities"))
    e <- IO(ctx.run(query[Entity].delete))
    _ <- IO(logger.info(s"Deleted entities count: $e"))
  } yield e
}

object EntityRepository {
  def apply(): EntityRepository = new EntityRepository()
}