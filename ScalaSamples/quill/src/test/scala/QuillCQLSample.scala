import io.getquill.{CassandraSyncContext, SnakeCase}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

class QuillCQLSample extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  lazy val ctx = new CassandraSyncContext(SnakeCase, "cassandra")

  import ctx._

  case class Entity(id: Long, value: String)

  implicit val entityMeta = schemaMeta[Entity]("entity")

  override protected def beforeAll(): Unit = {
    ctx.executeAction[Boolean]("create keyspace if not exists quill_test with replication={'class' : 'SimpleStrategy', 'replication_factor': '1'}")

    ctx.executeAction[Boolean](
      """create table if not exists quill_test.entity (
        |  id bigint,
        |  value text,
        |  primary key (id)
        |)""".stripMargin)
  }

  override protected def afterAll(): Unit = {
    ctx.executeAction[Boolean]("drop table if exists quill_test.entity")
  }

  override protected def beforeEach(): Unit = {
    ctx.executeAction[Boolean]("truncate table quill_test.entity")
  }

  test("simple insert") {
    ctx.run(query[Entity].insert(_.id -> 1, _.value -> "val1").ifNotExists)
    val r = ctx.run(query[Entity])

    assert(r.size == 1)
    assert(r.head == Entity(1, "val1"))
  }

  test("filter") {
    (1 to 10).foreach(i => {
      ctx.run(query[Entity].insert(_.id -> lift(i.toLong), _.value -> lift(s"val$i")).ifNotExists)
    })

    val r = ctx.run(query[Entity].filter(_.id <= 5).allowFiltering)

    assert(r.size == 5)
  }

  test("with ttl") {
    ctx.run(query[Entity].insert(_.id -> 1, _.value -> "val1").usingTtl(1))
    val r = ctx.run(query[Entity])

    assert(r.size == 1)
    assert(r.head == Entity(1, "val1"))
    Thread.sleep(1100)

    val v = ctx.run(query[Entity])

    assert(v.isEmpty)
  }

  test("update") {
    (1 to 10).foreach(i => {
      ctx.run(query[Entity].insert(_.id -> lift(i.toLong), _.value -> lift(s"val$i")).ifNotExists)
    })

    val r = ctx.run(query[Entity].filter(e => e.id == 5))
    assert(r.head == Entity(5, "val5"))

    ctx.run(query[Entity].filter(e => e.id == 5).update(_.value -> "updated").ifCond(_.value == "val5"))
    val v = ctx.run(query[Entity].filter(e => e.id == 5))
    assert(v.head == Entity(5, "updated"))
  }

  test("delete") {
    (1 to 5).foreach(i => {
      ctx.run(query[Entity].insert(_.id -> lift(i.toLong), _.value -> lift(s"val$i")).ifNotExists)
    })

    assert(ctx.run(query[Entity]).size == 5)
    ctx.run(query[Entity].filter(_.id == 1).delete.ifCond(_.value == "val1"))
    assert(ctx.run(query[Entity]).size == 4)
  }
}
