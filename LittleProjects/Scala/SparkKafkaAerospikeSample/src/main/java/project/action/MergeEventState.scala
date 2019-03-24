package project.action


import org.apache.logging.log4j.LogManager
import project.model.{Event, EventInfo}

class MergeEventState() extends Aggregate {
  @transient
  private val logger = LogManager.getLogger("MergeEventState")

  override def process(a: Event, b: Option[EventInfo]): EventInfo = {
    logger.info(s"Input event: $a")
    b match {
      case Some(info) => info.copy(count = info.count + 1, endTime = a.timestamp)
      case None => EventInfo(a.id, 1, a.timestamp, a.timestamp)
    }
  }
}

object MergeEventState {
  def apply(): MergeEventState = new MergeEventState()
}

