package akka.nifi.stream.postgres.service

import akka.nifi.stream.postgres.model.UnknownEvent
import akka.nifi.stream.postgres.repository.UnknownEventRepository

class UnknownEventService extends SQLService[UnknownEvent, Long] {
  override val repository = UnknownEventRepository()
}

object UnknownEventService {
  def apply(): UnknownEventService = new UnknownEventService()
}