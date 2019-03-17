package project.sink

import project.model.EventInfo

trait Sink {
  def save(o: EventInfo): Unit
}
