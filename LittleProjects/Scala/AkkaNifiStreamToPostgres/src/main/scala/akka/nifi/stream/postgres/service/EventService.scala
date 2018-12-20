package akka.nifi.stream.postgres.service

import akka.nifi.stream.postgres.model.Event
import akka.nifi.stream.postgres.repository.EventRepository

class EventService extends SQLService[Event, Long] {
  override val repository = EventRepository()
}

object EventService {
  def apply(): EventService = new EventService()
}