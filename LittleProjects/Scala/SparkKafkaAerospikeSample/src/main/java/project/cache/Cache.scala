package project.cache

import project.model.EventInfo

trait Cache {
  def get(key: Long): Option[EventInfo]

  def put(key: Long, value: EventInfo): Unit
}
