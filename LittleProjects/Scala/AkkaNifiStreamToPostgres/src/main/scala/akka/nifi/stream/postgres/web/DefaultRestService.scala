package akka.nifi.stream.postgres.web
import akka.nifi.stream.postgres.service.{ComplexService, EntityService, EventService, UnknownEventService}

class DefaultRestService extends RestApiService {
  override val entityService: EntityService = EntityService()
  override val eventService: EventService = EventService()
  override val unknownEventService: UnknownEventService = UnknownEventService()
  override val complexService: ComplexService = ComplexService()
}

object DefaultRestService {
  def apply(): DefaultRestService = new DefaultRestService()
}
