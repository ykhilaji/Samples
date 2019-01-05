import java.time.LocalDateTime

import io.getquill.{PostgresEscape, PostgresJdbcContext}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

class QuillSQLSample extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  lazy val ctx = new PostgresJdbcContext(PostgresEscape, "ctx")

  import ctx._
  implicit val entityMeta = schemaMeta[Entity]("quill", _.updateTime -> "update_time")

  /**
    * create table if not exists quill (
    * id    serial primary key,
    * value text,
    * update_time timestamp default now()
    * );
    */

  case class Entity(id: Long = 0, value: String, updateTime: Option[LocalDateTime] = None)

  override protected def beforeEach(): Unit = {
    ctx.run(query[Entity].delete)
  }

  override protected def beforeAll(): Unit = {
    ctx.executeAction[Boolean](
      """
        |create table if not exists quill (
        |  id    serial primary key,
        |  "value" text,
        |  update_time timestamp default now()
        |)
        |
      """.stripMargin)
  }

  override protected def afterAll(): Unit = {
    ctx.executeAction[Boolean]("drop table quill;")
  }

  test("simple select") {
    val digit = quote(1)
    assert(ctx.run(digit) == 1)
  }

  test("raw sql") {
    val sql = quote {
      infix"select 2 * 2".as[Int]
    }
    assert(ctx.run(sql) == 4)
  }

  test("insert/select/update") {
    implicit val entityInsertMeta = insertMeta[Entity](_.id) // exclude id from insert values

    ctx.run(query[Entity].insert(lift(Entity(value = "val1"))))
    ctx.run(query[Entity].insert(lift(Entity(value = "val2"))))
    ctx.run(query[Entity].insert(lift(Entity(value = "val3"))))

    val result = ctx.run(query[Entity].filter(_.value == "val1"))

    assert(result.size == 1)
    assert(result.head.value == "val1")

    implicit val entityUpdateMeta: ctx.UpdateMeta[Entity] = updateMeta[Entity](_.id, _.updateTime) // exclude columns from update
    ctx.run(query[Entity].filter(_.id == lift(result.head.id)).update(_.value -> "updated"))
    val updated = ctx.run(query[Entity].filter(_.id == lift(result.head.id)))

    assert(updated.head.value == "updated")
  }

  test("transaction") {
    ctx.transaction {
      ctx.run(query[Entity].insert(lift(Entity(value = "val1"))))
    }

    assert(ctx.run(infix"select count(*) from quill".as[Int]) == 1)
  }
}
