package akka.nifi.stream.postgres.web

import akka.nifi.stream.postgres.model.{Entity, Event, UnknownEvent}
import akka.nifi.stream.postgres.service.{ComplexService, EntityService, EventService, UnknownEventService}
import akka.nifi.stream.postgres.web.HttpListener.NewEvent

trait RestApiService {
  val entityService: EntityService
  val eventService: EventService
  val unknownEventService: UnknownEventService
  val complexService: ComplexService

  def newEvent(e: NewEvent): Either[Throwable, Unit] = complexService.nexEvent(e.id)

  def findEntityById(id: Long): Either[Throwable, Option[Entity]] = entityService.findOne(id)

  def isEntityExists(id: Long): Either[Throwable, Boolean] = entityService.exists(id)

  def findEventById(id: Long): Either[Throwable, Option[Event]] = eventService.findOne(id)

  def fundUnknownEventById(id: Long): Either[Throwable, Option[UnknownEvent]] = unknownEventService.findOne(id)

  def findAllEntitiyes(): Either[Throwable, Seq[Entity]] = entityService.findAll()

  def findAllEvents(): Either[Throwable, Seq[Event]] = eventService.findAll()

  def findAllUnknownEvents(): Either[Throwable, Seq[UnknownEvent]] = unknownEventService.findAll()

  def deleteEntity(id: Long): Either[Throwable, Unit] = entityService.delete(id)

  def deleteEvent(id: Long): Either[Throwable, Unit] = eventService.delete(id)

  def deleteUnknownEvent(id: Long): Either[Throwable, Unit] = unknownEventService.delete(id)

  def saveEntity(e: Entity): Either[Throwable, Entity] = entityService.save(e)

  def saveEvent(e: Event): Either[Throwable, Event] = eventService.save(e)

  def saveUnknownEvent(e: UnknownEvent): Either[Throwable, UnknownEvent] = unknownEventService.save(e)

  def updateEntity(e: Entity): Either[Throwable, Entity] = entityService.update(e)

  def updateEvent(e: Event): Either[Throwable, Event] = eventService.update(e)

  def updateUnknownEvent(e: UnknownEvent): Either[Throwable, UnknownEvent] = unknownEventService.update(e)

  def findEntityWithNoUnknownEvents(id: Long): Either[Throwable, Option[Entity]] = complexService.findEntityWithNoUnknownEvents(id)

  def findEntityWithNoEvents(id: Long): Either[Throwable, Option[Entity]] = complexService.findEntityWithNoEvents(id)
}
