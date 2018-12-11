import java.util.concurrent.TimeUnit

import akka.mongo.crud.configuration.DataSource
import akka.mongo.crud.model.{CountByTypeAggregation, Event, EventSource}
import akka.mongo.crud.repository.{EventRepository, EventSourceRepository}
import org.mongodb.scala.MongoCollection
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits._

class RepositoryTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll with SequentialNestedSuiteExecution {
  class EventRepositoryTest extends EventRepository {
    override val collection: MongoCollection[Event] = DataSource.db.getCollection("eventTest")
  }

  class EventSourceRepositoryTest extends EventSourceRepository {
    override val collection: MongoCollection[EventSource] = DataSource.db.getCollection("sourceTest")
  }

  val eventRepository = new EventRepositoryTest()
  val eventSourceRepository = new EventSourceRepositoryTest()

  override protected def beforeEach(): Unit = {
    val dropEvents = eventRepository.collection.drop().toFuture()
    val dropSources = eventSourceRepository.collection.drop().toFuture()

    Await.result(for {
      _ <- dropEvents
      _ <- dropSources
      _ <- DataSource.db.getCollection("countByType").drop().toFuture()
    } yield (), Duration(1, TimeUnit.SECONDS))
  }

  override protected def afterAll(): Unit = DataSource.mongoClient.close()

  test("find by object id") {
    val eventSource = EventSource("test", "test")
    val r = for {
      _ <- eventSourceRepository.insert(eventSource)
      f <- eventSourceRepository.find(eventSource._id)
    } yield f

    assert(eventSource == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)).orNull)
  }

  test("find event source by name") {
    val e = EventSource("test", "test")

    val r = for {
      _ <- eventSourceRepository.insert(e)
      f <- eventSourceRepository.findByName("test")
    } yield f

    assert(e == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)).orNull)
  }

  test("find all event sources (asc)") {
    val e1 = EventSource("test", "t1")
    val e2 = EventSource("test", "t2")
    val e3 = EventSource("test", "t3")

    val r = for {
      _ <- eventSourceRepository.insert(e1)
      _ <- eventSourceRepository.insert(e2)
      _ <- eventSourceRepository.insert(e3)
      f <- eventSourceRepository.findAllSortByNameAsc()
    } yield f

    assert(Seq(e1, e2, e3) == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("find all event sources (desc)") {
    val e1 = EventSource("test", "t1")
    val e2 = EventSource("test", "t2")
    val e3 = EventSource("test", "t3")

    val r = for {
      _ <- eventSourceRepository.insert(e1)
      _ <- eventSourceRepository.insert(e2)
      _ <- eventSourceRepository.insert(e3)
      f <- eventSourceRepository.findAllSortByNameDesc()
    } yield f

    assert(Seq(e3, e2, e1) == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("find by name regex") {
    val e1 = EventSource("test", "a")
    val e2 = EventSource("test", "ab")
    val e3 = EventSource("test", "abc")
    val e4 = EventSource("test", "abcd")

    val r = for {
      _ <- eventSourceRepository.insert(e1)
      _ <- eventSourceRepository.insert(e2)
      _ <- eventSourceRepository.insert(e3)
      _ <- eventSourceRepository.insert(e4)
      f <- eventSourceRepository.findByNameRegex("ab.+")
    } yield f

    assert(Seq(e3, e4) == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("count event sources") {
    val e1 = EventSource("test", "a")
    val e2 = EventSource("test", "ab")
    val e3 = EventSource("test", "abc")

    val r = for {
      _ <- eventSourceRepository.insert(e1)
      _ <- eventSourceRepository.insert(e2)
      _ <- eventSourceRepository.insert(e3)
      f <- eventSourceRepository.count()
    } yield f

    assert(3 == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("delete event source by name") {
    val e1 = EventSource("test", "a")
    val e2 = EventSource("test", "ab")
    val e3 = EventSource("test", "abc")

    val r = for {
      _ <- eventSourceRepository.insert(e1)
      _ <- eventSourceRepository.insert(e2)
      _ <- eventSourceRepository.insert(e3)
      _ <- eventSourceRepository.deleteByName("a")
      f <- eventSourceRepository.count()
    } yield f

    assert(2 == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("delete event source by name (regex)") {
    val e1 = EventSource("test", "a")
    val e2 = EventSource("test", "ab")
    val e3 = EventSource("test", "abc")

    val r = for {
      _ <- eventSourceRepository.insert(e1)
      _ <- eventSourceRepository.insert(e2)
      _ <- eventSourceRepository.insert(e3)
      _ <- eventSourceRepository.deleteByNameRegex("a.+")
      f <- eventSourceRepository.count()
    } yield f

    assert(1 == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("count events by type") {
    val s = EventSource("test", "a")
    val e1 = Event("a", "_", s._id)
    val e2 = Event("b", "_", s._id)
    val e3 = Event("c", "_", s._id)
    val e4 = Event("a", "_", s._id)
    val e5 = Event("b", "_", s._id)

    val r = for {
      _ <- eventSourceRepository.insert(s)
      _ <- eventRepository.insert(e1)
      _ <- eventRepository.insert(e2)
      _ <- eventRepository.insert(e3)
      _ <- eventRepository.insert(e4)
      _ <- eventRepository.insert(e5)
      f <- eventRepository.countByType("a")
    } yield f

    assert(CountByTypeAggregation("a", 2) == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)).orNull)
  }

  test("find events by type") {
    val s = EventSource("test", "a")
    val e1 = Event("a", "_", s._id)
    val e2 = Event("b", "_", s._id)
    val e3 = Event("c", "_", s._id)
    val e4 = Event("a", "_", s._id)
    val e5 = Event("b", "_", s._id)

    val r = for {
      _ <- eventSourceRepository.insert(s)
      _ <- eventRepository.insert(e1)
      _ <- eventRepository.insert(e2)
      _ <- eventRepository.insert(e3)
      _ <- eventRepository.insert(e4)
      _ <- eventRepository.insert(e5)
      f <- eventRepository.findByType("a")
    } yield f

    assert(List(e1, e4) == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }

  test("find events by event source") {
    val s1 = EventSource("test", "a")
    val s2 = EventSource("test", "b")
    val e1 = Event("a", "_", s1._id)
    val e2 = Event("b", "_", s2._id)
    val e3 = Event("c", "_", s1._id)
    val e4 = Event("a", "_", s2._id)
    val e5 = Event("b", "_", s1._id)

    val r = for {
      _ <- eventSourceRepository.insert(s1)
      _ <- eventSourceRepository.insert(s2)
      _ <- eventRepository.insert(e1)
      _ <- eventRepository.insert(e2)
      _ <- eventRepository.insert(e3)
      _ <- eventRepository.insert(e4)
      _ <- eventRepository.insert(e5)
      f <- eventRepository.findByEventSource(s2._id)
    } yield f

    assert(List(e2, e4) == Await.result(r.unsafeRunSync(), Duration(1, TimeUnit.SECONDS)))
  }
}
