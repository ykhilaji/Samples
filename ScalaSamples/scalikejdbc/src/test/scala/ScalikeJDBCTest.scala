import org.scalatest._
import scalikejdbc._

case class Row(id: Int, value: String)

object Row extends SQLSyntaxSupport[Row] {
  override def schemaName: Option[String] = Some("public")

  override def tableName: String = "test"

  override def columns: Seq[String] = Seq("id", "value")

  // r.id and r.value - dynamic values (Type Dynamic (SIP-17) since Scala 2.10.0)
  def apply(r: ResultName[Row])(rs: WrappedResultSet): Row = Row(rs.get[Int](r.id), rs.get[String](r.value))

  def apply(rs: WrappedResultSet): Row = Row(rs.get[Int](1), rs.get[String](2))
}

case class Row2(id: Int, value: String)

object Row2 extends SQLSyntaxSupport[Row2] {
  override def schemaName: Option[String] = Some("public")

  override def tableName: String = "test2"

  override def columns: Seq[String] = Seq("id", "value")

  // r.id and r.value - dynamic values (Type Dynamic (SIP-17) since Scala 2.10.0)
  def apply(r: ResultName[Row2])(rs: WrappedResultSet): Row2 = Row2(rs.get[Int](r.id), rs.get[String](r.value))

  def apply(rs: WrappedResultSet): Row2 = Row2(rs.get[Int](1), rs.get[String](2))
}

class ScalikeJDBCTest extends FunSuite with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val settings = ConnectionPoolSettings(
      initialSize = 1, maxSize = 1, connectionTimeoutMillis = 1000, validationQuery = "select 1", driverName = "org.postgresql.Driver"
    )
    ConnectionPool.singleton("jdbc:postgresql://192.168.99.100:5432/postgres", "postgres", "", settings)

    DB localTx {
      implicit session => {
        sql"create table if not exists test(id int primary key, value text)".execute.apply()
        sql"create table if not exists test2(id int primary key, value text)".execute.apply()
      }
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    DB localTx {
      implicit session => {
        sql"truncate table test".execute().apply()
        sql"truncate table test2".execute().apply()
      }
    }
  }

  test("simple select") {
    assertCompiles("""""")
    val result = DB readOnly {
      implicit session => {
        sql"select 1"
      }.map(rs => rs.get[Int](1)).single().apply()
    }

    assert(result.getOrElse(0) == 1)
  }

  test("select first row from multiple rows") {
    val result = DB readOnly {
      implicit session => {
        sql"select 1 union select 2 union select 3".map(rs => rs.get[Int](1)).first().apply()
      }
    }

    assert(result.getOrElse(0) == 1)
  }

  test("select multiple rows") {
    val result = DB readOnly {
      implicit session => {
        sql"select 1 union select 2 union select 3".map(rs => rs.get[Int](1)).list.apply()
      }
    }

    assert(result == List(1, 2, 3))
  }

  test("use connection explicitly") {
    val result = using(ConnectionPool.borrow()) {
      connection => {
        val rs = connection.createStatement().executeQuery("select 1, 2, 3")
        rs.next()
        (rs.getInt(1), rs.getInt(2), rs.getInt(3))
      }
    }

    assert(result == (1, 2, 3))
  }

  test("insert value in table") {
    DB localTx {
      implicit session => {
        sql"insert into test values(1, 'value1')".update().apply()
        sql"insert into test values(2, 'value2')".update().apply()
        sql"insert into test values(3, 'value3')".update().apply()
      }
    }

    val result = DB readOnly {
      implicit session => {
        sql"select id, value from test order by id asc".map(rs => (rs.get[Int](1), rs.get[String](2))).list.apply()
      }
    }

    assert(result == List((1, "value1"), (2, "value2"), (3, "value3")))
  }

  // each block - separate transaction
  test("delete values") {
    DB localTx {
      implicit session => {
        sql"insert into test values(1, 'value1')".update().apply()
        sql"insert into test values(2, 'value2')".update().apply()
        sql"insert into test values(3, 'value3')".update().apply()
      }
    }

    val result = DB readOnly {
      implicit session => {
        sql"select id, value from test order by id asc".map(rs => (rs.get[Int](1), rs.get[String](2))).list.apply()
      }
    }

    assert(result == List((1, "value1"), (2, "value2"), (3, "value3")))

    DB localTx {
      implicit session => {
        sql"delete from test where id in (2, 3)".execute().apply()
      }
    }

    val resultAfterDelete = DB readOnly {
      implicit session => {
        sql"select id, value from test".map(rs => (rs.get[Int](1), rs.get[String](2))).list.apply()
      }
    }

    assert(resultAfterDelete == List((1, "value1")))
  }

  test("update value") {
    DB localTx {
      implicit session => {
        sql"insert into test values(1, 'value1')".update().apply()
        sql"insert into test values(2, 'value2')".update().apply()
        sql"insert into test values(3, 'value3')".update().apply()
      }
    }

    DB localTx {
      implicit session => {
        sql"update test set value='updated' where id=1".update().apply()
      }
    }

    val result = DB readOnly {
      implicit session => {
        sql"select id, value from test order by id asc".map(rs => (rs.get[Int](1), rs.get[String](2))).list.apply()
      }
    }

    assert(result == List((1, "updated"), (2, "value2"), (3, "value3")))
  }

  test("batch insert") {
    DB localTx {
      implicit session => {
        sql"insert into test values (?, ?)".batch(Seq(Seq(1, "v1"), Seq(2, "v2"), Seq(3, "v3")): _*).apply()
      }
    }

    val result = DB readOnly {
      implicit session => {
        sql"select id, value from test order by id asc".map(rs => (rs.get[Int](1), rs.get[String](2))).list.apply()
      }
    }

    assert(result == List((1, "v1"), (2, "v2"), (3, "v3")))
  }

  test("sql syntax") {
    DB localTx {
      implicit session => {
        sql"insert into test values(1, 'value1')".update().apply()
      }
    }
    val result = DB readOnly {
      implicit session => {
        val ordering = sqls"order by id asc"
        sql"select id, value from test ${ordering}".map(rs => (rs.get[Int](1), rs.get[String](2))).list.apply()
      }
    }

    assert(result == List((1, "value1")))
  }

  test("sql syntax support select") {
    val r = Row.syntax("r")
    DB localTx {
      implicit session =>
        withSQL {
          insert.into(Row).values(1, "value")
        }.update.apply()
    }

    val result = DB readOnly {
      implicit session =>
        withSQL {
          select(r.*).from(Row as r)
        }.map(Row(_)).single().apply()
    }

    assert(result.orNull == Row(1, "value"))
  }

  test("sql syntax support update") {
    val r = Row.syntax("r")
    val result = DB localTx {
      implicit session =>
        withSQL {
          insert.into(Row).values(1, "value")
        }.update.apply()
        withSQL {
          select(r.*).from(Row as r)
        }.map(Row(_)).single().apply()
    }

    assert(result.orNull == Row(1, "value"))

    val resultUpdated = DB localTx {
      implicit session => {
        withSQL {
          update(Row).set(Row.column.value -> "updated")
        }.update().apply()
        withSQL {
          select(r.*).from(Row as r)
        }.map(Row(_)).single().apply()
      }
    }

    assert(resultUpdated.orNull == Row(1, "updated"))
  }

  test("sql syntax support delete") {
    val r = Row.syntax("r")
    val result = DB localTx {
      implicit session =>
        withSQL {
          insert.into(Row).values(1, "value")
        }.update.apply()
        withSQL {
          select(r.*).from(Row as r)
        }.map(Row(_)).single().apply()
    }

    assert(result.orNull == Row(1, "value"))
    DB localTx {
      implicit session => {
        withSQL {
          delete.from(Row as r).where.eq(Row.column.id, 1)
        }.update().apply()
      }
    }
    val resultUpdated = DB localTx {
      implicit session => {
        withSQL {
          select(r.*).from(Row as r).where.eq(Row.column.id, 1)
        }.map(Row(_)).single().apply()
      }
    }

    assert(resultUpdated.orNull == null)
  }

  test("sql syntax support join") {
    val t = Row.syntax("t")
    val t1 = Row2.syntax("t1")

    val result = DB localTx {
      implicit session => {
        withSQL {
          insert.into(Row).values(1, "value1")
        }.update().apply()
        withSQL {
          insert.into(Row).values(2, "value2")
        }.update().apply()
        withSQL {
          insert.into(Row).values(3, "value3")
        }.update().apply()
        withSQL {
          insert.into(Row2).values(3, "3value")
        }.update().apply()
        withSQL {
          insert.into(Row2).values(2, "2value")
        }.update().apply()
        withSQL {
          insert.into(Row2).values(1, "1value")
        }.update().apply()

        withSQL {
          select(t.*, t1.*).from(Row as t).innerJoin(Row2 as t1).on(t.id, t1.id).where.eq(t.id, 1)
        }.map(rs => (rs.get[Int](1), rs.get[String](2), rs.get[Int](3), rs.get[String](4))).single().apply()
      }
    }

    assert(result.orNull == (1, "value1", 1 ,"1value"))
  }
}
