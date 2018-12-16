package akka.infinispan.crud.service

import akka.infinispan.crud.model.Entity
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

class EntityServiceTest extends FunSuite
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
  val service = EntityService()

  override protected def beforeAll(): Unit = super.beforeAll()

  override protected def afterAll(): Unit = super.afterAll()

  override protected def afterEach(): Unit = service.deleteAll()

  test("find by id") {
    val e = Entity(1, "value")
    service.save(e)
    val r = service.findOne(1)
    assert(r.isRight)
    assert(r.right.get.contains(e))
  }

  test("find by id - none") {
    assert(service.findOne(1).right.get.isEmpty)
  }

  test("find all") {
    val e1 = Entity(1, "value")
    val e2 = Entity(2, "value")
    service.saveAll(List(e1, e2))
    val r = service.findAll()
    assert(r.isRight)
    assert(r.right.get == List(e1, e2))
  }

  test("LRU") {
    service.saveAll((1 to 20).map(i => Entity(i, i.toString)))
    val r = service.findAll()
    assert(r.isRight)
    assert(r.right.get.size == 10)
  }
}
