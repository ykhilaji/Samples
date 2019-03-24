package project.action

import project.model.{Event, EventInfo}

trait Aggregate {
  def process(a: Event, b: Option[EventInfo]): EventInfo
}
