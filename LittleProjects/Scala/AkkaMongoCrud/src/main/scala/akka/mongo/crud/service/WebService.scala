package akka.mongo.crud.service

import akka.mongo.crud.model.{CountByTypeAggregation, Event, EventSource}
import akka.mongo.crud.repository.{EventRepository, EventSourceRepository}
import akka.mongo.crud.web.{EventRequest, EventSourceRequest}
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.result.DeleteResult

import scala.concurrent.Future

class WebService {
  val eventRepository = EventRepository()
  val eventSourceRepository = EventSourceRepository()

  def saveEventSource(es: EventSourceRequest): Future[Completed] = eventSourceRepository.insert(EventSource(es.description, es.name)).unsafeRunSync()

  def saveEvent(e: EventRequest): Future[Completed] = eventRepository.insert(Event(e.`type`, e.description, e._id)).unsafeRunSync()

  def findAllEventSources(): Future[Seq[EventSource]] = eventSourceRepository.findAll().unsafeRunSync()

  def findAllEventsByType(`type`: String): Future[Seq[Event]] = eventRepository.findByType(`type`).unsafeRunSync()

  def findAllEventsByEventSource(_id: ObjectId): Future[Seq[Event]] = eventRepository.findByEventSource(_id).unsafeRunSync()

  def updateEvent(e: EventRequest): Future[Completed] = eventRepository.update(Event(e.`type`, e.description, e._id)).unsafeRunSync()

  def updateEventSource(es: EventSourceRequest): Future[Completed] = eventSourceRepository.update(EventSource(es.description, es.name)).unsafeRunSync()

  def findEventSourceByName(name: String): Future[Option[EventSource]] = eventSourceRepository.findByName(name).unsafeRunSync()

  def findEventSourceByRegex(pattern: String): Future[Seq[EventSource]] = eventSourceRepository.findByNameRegex(pattern).unsafeRunSync()

  def countEventSource(): Future[Long] = eventSourceRepository.count().unsafeRunSync()

  def countEventsByType(`type`: String): Future[Option[CountByTypeAggregation]] = eventRepository.countByType(`type`).unsafeRunSync()

  def deleteEvent(_id: ObjectId): Future[DeleteResult] = eventRepository.delete(_id).unsafeRunSync()

  def deleteEventSource(_id: ObjectId): Future[Unit] = (for {
    _ <- eventRepository.deleteByEventSource(_id)
    _ <- eventSourceRepository.delete(_id)
  } yield ()).unsafeToFuture()
}

object WebService {
  def apply(): WebService = new WebService()
}