package akka.nifi.stream.postgres.service

import java.time.LocalDateTime

import akka.nifi.stream.postgres.model.{Entity, Event, UnknownEvent}
import akka.nifi.stream.postgres.repository.{EntityRepository, EventRepository, UnknownEventRepository}
import cats.data.OptionT
import cats.effect.IO
import org.apache.logging.log4j.{LogManager, Logger}
import scalikejdbc._

import scala.util.Try

class ComplexService {
  val logger: Logger = LogManager.getLogger("complex-service")
  val eventRepository: EventRepository = EventRepository()
  val entityRepository: EntityRepository = EntityRepository()
  val unknownEventRepository: UnknownEventRepository = UnknownEventRepository()

  def findEntityWithNoUnknownEvents(entityId: Long): Either[Throwable, Option[Entity]] = Try {
    DB localTx {
      implicit session => {
        (for {
          _ <- OptionT.liftF(IO(logger.info("Finding entity by id which has no unknown events related to it")))
          entity <- entityRepository.findOne(entityId)
          unknownEvents <- OptionT.liftF(unknownEventRepository.findAllByEntityId(entity.id))
          if unknownEvents.isEmpty // just for sample
        } yield entity).value.unsafeRunSync()
      }
    }
  }.toEither

  def findEntityWithNoEvents(entityId: Long): Either[Throwable, Option[Entity]] = Try {
    DB localTx {
      implicit session => {
        (for {
          _ <- OptionT.liftF(IO(logger.info("Finding entity by id which has no known events related to it")))
          entity <- entityRepository.findOne(entityId)
          events <- OptionT.liftF(eventRepository.findAllByEntityId(entity.id))
          if events.isEmpty // just for sample
        } yield entity).value.unsafeRunSync()
      }
    }
  }.toEither

  def nexEvent(entityId: Long): Either[Throwable, Unit] = Try[Unit] {
    DB localTx {
      implicit session => {
        entityRepository.findOne(entityId).value.flatMap(e => {
          if (e.isDefined) {
            eventRepository.save(Event(0, Some(LocalDateTime.now()), entityId))
          } else {
            unknownEventRepository.save(UnknownEvent(0, Some(LocalDateTime.now()), entityId))
          }
        }).unsafeRunSync()
      }
    }
  }.toEither
}

object ComplexService {
  def apply(): ComplexService = new ComplexService()
}
