package project


import org.scalatest.{FunSuite, Matchers}
import project.action.MergeEventState
import project.model.{Event, EventInfo}

class MergeEventStateTest extends FunSuite with Matchers {

  test("merge empty state") {
    val action = MergeEventState()
    val event = Event(1, "value", 0)
    val state = action.process(event, None)
    assertResult(EventInfo(1, 1, event.timestamp, event.timestamp))(state)
  }

  test("merge existing state") {
    val action = MergeEventState()
    val oldState = EventInfo(1, 1, 0, 0)
    val event = Event(1, "value", 0)
    val state = action.process(event, Some(oldState))
    assertResult(EventInfo(1, 2, oldState.startTime, event.timestamp))(state)

  }
}
