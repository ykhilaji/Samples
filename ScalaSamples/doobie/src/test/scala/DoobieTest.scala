import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.concurrent.ExecutionContext


case class Entity(id: Long, value: String)

class DoobieTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  implicit val cs = IO.contextShift(ExecutionContext.global)
  lazy val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://192.168.99.100:5432/postgres",
    user = "postgres",
    pass = ""
  )

  override protected def beforeAll(): Unit = {
    sql"""
         |create table if not exists public.entity (
         |id numeric(38) primary key,
         |value text
         |)
       """.stripMargin.update.run.transact(xa).unsafeRunSync()
  }

  override protected def afterEach(): Unit = {
    sql"truncate table public.entity".update.run.transact(xa).unsafeRunSync()
  }

  test("constant") {
    val constant = "value".pure[ConnectionIO]
    val result = constant.transact(xa).unsafeRunSync()
    assert(result == "value")
  }

  test("simple select query") {
    val result = sql"select 1, 2, 3".query[(Int, Int, Int)].unique.transact(xa).unsafeRunSync()
    assert(result == (1, 2, 3))
  }

  test("single transaction - multiple operations") {
    val result: Int = (for {
      a <- sql"select 1".query[Int].unique
      b <- sql"select 1, 2".query[(Int, Int)].unique
      c <- sql"select 1, 2, 3".query[(Int, Int, Int)].unique
    } yield (a + b._1 + b._2 + c._1 + c._2 + c._3)).transact(xa).unsafeRunSync()

    assert(10 == result)
  }

  test("select multiple rows") {
    val result =
      sql"""
           |select 1
           |union all
           |select 2
           |union all
           | select 3
         """.stripMargin
        .query[Int]
        .to[List]
        .transact(xa) // .take(2) will select all rows from table and return 2
        .unsafeRunSync

    assert(result == List(1, 2, 3))
  }

  test("select multiple rows using stream") {
    val result =
      sql"""
           |select 1
           |union all
           |select 2
           |union all
           | select 3
         """.stripMargin
        .query[Int]
        .stream // .take(2) will select only 2 rows (close stream after 2 rows have been emitted). LIMIT on DB side will produce the same result
        .compile
        .toList
        .transact(xa)
        .unsafeRunSync

    assert(result == List(1, 2, 3))
  }

  test("select -> case class (Entity)") {
    val result = sql"select 1, 'value'".query[Entity].unique.transact(xa).unsafeRunSync()
    assert(result == Entity(1, "value"))
  }

  test("prepared statements") {
    val filter = 3
    val result =
      sql"""
           |select r.id, r.value from (
           |select 1 as id, 'value' as value
           |union all
           |select 2 as id, 'value' as value
           |union all
           |select 3 as id, 'value' as value
           |union all
           |select 4 as id, 'value' as value
           |union all
           |select 5 as id, 'value' as value
           |) r where r.id > $filter
         """.stripMargin
      .query[Entity]
      .to[List]
      .transact(xa)
      .unsafeRunSync()

    assert(result == List(Entity(4, "value"), Entity(5, "value")))
  }

  test("insert") {
    val id = 1
    val value = "value"

    val result = (for {
      count <- sql"select count(*) from public.entity".query[Int].unique
      _ <- sql"insert into public.entity(id, value) values($id, $value)".update.run
      countAfter <- sql"select count(*) from public.entity".query[Int].unique
    } yield (count, countAfter)).transact(xa).unsafeRunSync()

    assert(result._1 == 0)
    assert(result._2 == 1)
  }

}
