package project.model

/**
  * Aggregated info about particular event
  * @param id - event id
  * @param count - amount of events with the same id
  * @param startTime - first event time in window
  * @param endTime - last event time in window (may be equal to start time)
  */
case class EventInfo(id: Long, count: Long, startTime: Long, endTime: Long)
