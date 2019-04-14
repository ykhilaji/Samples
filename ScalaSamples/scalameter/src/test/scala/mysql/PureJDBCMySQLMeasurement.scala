package mysql

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.scalameter.api._

import scala.util.Random

/**
  * cores: 4
  * name: Java HotSpot(TM) 64-Bit Server VM
  * osArch: amd64
  * osName: Windows 7
  * vendor: Oracle Corporation
  * version: 25.131-b11
  */
object PureJDBCMySQLMeasurement extends Bench.LocalTime {
  val URL = "jdbc:mysql://192.168.99.100:3306/mysql"
  val USER = "root"
  val PASSWORD = "root"
  val DRIVER = "com.mysql.jdbc.Driver"
  val INSERT_TEST = "insert into test(id, value) values(?, ?)"
  val INSERT_TEST_JOIN = "insert into test_join(id) values(?)"
  val SELECT_TEST = "select id, value from test where id = ?"
  val SELECT_JOIN_TEST = "select t.id, t.value from test t inner join test_join tj on t.id=tj.id where t.id = ?"

  val records = Gen.exponential("records")(1000, 10000, 10)
  val batchSize = Gen.exponential("batchSize")(10, 1000, 10)
  val gen = Gen.crossProduct(records, batchSize)

  var connection: Connection = _
  var statement: PreparedStatement = _

  performance of "insert" in {
    /*
Parameters(records -> 1000): 1685.640395 ms
Parameters(records -> 10000): 8344.808949 ms
     */
    measure method "ordered insert single records" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        statement.close()
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { el =>
        statement = connection.prepareStatement(INSERT_TEST)
        (0 to el).foreach(i => {
          statement.setLong(1, i)
          statement.setLong(2, i)
          statement.execute()
        })
      }
    }

    /*
Parameters(records -> 1000, batchSize -> 10): 397.970444 ms
Parameters(records -> 1000, batchSize -> 100): 457.243797 ms
Parameters(records -> 1000, batchSize -> 1000): 384.388633 ms
Parameters(records -> 10000, batchSize -> 10): 3983.095315 ms
Parameters(records -> 10000, batchSize -> 100): 3740.783451 ms
Parameters(records -> 10000, batchSize -> 1000): 11669.085645 ms
     */
    measure method "ordered insert batch" in {
      using(gen) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { recordsBatch =>
        val batchSize = recordsBatch._2
        var currSize = 0
        statement = connection.prepareStatement(INSERT_TEST)

        (0 to recordsBatch._1).foreach(i => {
          statement.setLong(1, i)
          statement.setLong(2, i)
          statement.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            statement.executeBatch()
            currSize = 0
          }
        })

        statement.executeBatch()
      }
    }

    /*
Parameters(records -> 1000): 1608.284326 ms
Parameters(records -> 10000): 16308.998544 ms
 */
    measure method "ordered insert single records - table with 2 indexes" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        statement.close()
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { el =>
        statement = connection.prepareStatement(INSERT_TEST)
        (0 to el).foreach(i => {
          statement.setLong(1, i)
          statement.setLong(2, i)
          statement.execute()
        })
      }
    }

    /*
Parameters(records -> 1000, batchSize -> 10): 733.842595 ms
Parameters(records -> 1000, batchSize -> 100): 620.225821 ms
Parameters(records -> 1000, batchSize -> 1000): 586.210027 ms
Parameters(records -> 10000, batchSize -> 10): 4591.078242 ms
Parameters(records -> 10000, batchSize -> 100): 3731.893391 ms
Parameters(records -> 10000, batchSize -> 1000): 10936.347678 ms
     */
    measure method "ordered insert batch - table with 2 indexes" in {
      using(gen) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { recordsBatch =>
        val batchSize = recordsBatch._2
        var currSize = 0
        statement = connection.prepareStatement(INSERT_TEST)

        (0 to recordsBatch._1).foreach(i => {
          statement.setLong(1, i)
          statement.setLong(2, i)
          statement.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            statement.executeBatch()
            currSize = 0
          }
        })

        statement.executeBatch()
      }
    }

    /*
Parameters(records -> 1000): 1510.941577 ms
Parameters(records -> 10000): 16941.007672 ms
 */
    measure method "not ordered insert single records - table with 2 indexes" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)
        Random.setSeed(System.currentTimeMillis())
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        statement.close()
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { el =>
        statement = connection.prepareStatement(INSERT_TEST)
        (0 to el).foreach(_ => {
          statement.setLong(1, Random.nextInt(Int.MaxValue))
          statement.setLong(2, Random.nextInt(Int.MaxValue))
          statement.execute()
        })
      }
    }

    /*
Parameters(records -> 1000, batchSize -> 10): 635.497981 ms
Parameters(records -> 1000, batchSize -> 100): 564.225308 ms
Parameters(records -> 1000, batchSize -> 1000): 539.411458 ms
Parameters(records -> 10000, batchSize -> 10): 4782.592465 ms
Parameters(records -> 10000, batchSize -> 100): 3717.123363 ms
Parameters(records -> 10000, batchSize -> 1000): 3727.145914 ms
     */
    measure method "not ordered insert batch - table with 2 indexes" in {
      using(gen) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)
        Random.setSeed(System.currentTimeMillis())
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { recordsBatch =>
        val batchSize = recordsBatch._2
        var currSize = 0
        statement = connection.prepareStatement(INSERT_TEST)

        (0 to recordsBatch._1).foreach(_ => {
          statement.setLong(1, Random.nextInt(Int.MaxValue))
          statement.setLong(2, Random.nextInt(Int.MaxValue))
          statement.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            statement.executeBatch()
            currSize = 0
          }
        })

        statement.executeBatch()
      }
    }
  }

  performance of "simple select" in {
    /*
Parameters(records -> 1000): 9949.303024 ms
Parameters(records -> 10000): 96273.852057 ms
 */
    measure method "select" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)

        val batchSize = 1000
        var currSize = 0
        val s = connection.prepareStatement(INSERT_TEST)
        (0 to 10000).foreach(i => {
          s.setLong(1, i)
          s.setLong(2, i)
          s.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            s.executeBatch()
            currSize = 0
          }
        })

        s.executeBatch()
        s.close()
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { records =>
        statement = connection.prepareStatement(SELECT_TEST)

        (0 to records).foreach(i => {
          statement.setLong(1, i)
          val set = statement.executeQuery()
          set.next()
          val (id, value) = (set.getLong(1), set.getLong(2))
        })
      }
    }

    /*
Parameters(records -> 1000): 411.351821 ms
Parameters(records -> 10000): 3389.126523 ms
     */
    measure method "select with index" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)

        val batchSize = 1000
        var currSize = 0
        val s = connection.prepareStatement(INSERT_TEST)
        (0 to 10000).foreach(i => {
          s.setLong(1, i)
          s.setLong(2, i)
          s.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            s.executeBatch()
            currSize = 0
          }
        })

        s.executeBatch()
        s.close()
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { records =>
        statement = connection.prepareStatement(SELECT_TEST)

        (0 to records).foreach(i => {
          statement.setLong(1, i)
          val set = statement.executeQuery()
          set.next()
          val (id, value) = (set.getLong(1), set.getLong(2))
        })
      }
    }
  }

  performance of "joined select" in {
    /*
Parameters(records -> 1000): 20391.896633 ms
Parameters(records -> 10000): 193066.060814 ms
 */
    measure method "select" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.createStatement().execute("create table test_join(id numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.createStatement().execute("drop table test_join")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)

        val batchSize = 1000
        var currSize = 0
        val s = connection.prepareStatement(INSERT_TEST)
        val j = connection.prepareStatement(INSERT_TEST_JOIN)
        (0 to 10000).foreach(i => {
          s.setLong(1, i)
          s.setLong(2, i)
          j.setLong(1, i)
          s.addBatch()
          j.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            s.executeBatch()
            j.executeBatch()
            currSize = 0
          }
        })

        s.executeBatch()
        j.executeBatch()
        s.close()
        j.close()
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.createStatement().execute("truncate table test_join")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { records =>
        statement = connection.prepareStatement(SELECT_JOIN_TEST)

        (0 to records).foreach(i => {
          statement.setLong(1, i)
          val set = statement.executeQuery()
          set.next()
          val (id, value) = (set.getLong(1), set.getLong(2))
        })
      }
    }

    /*
Parameters(records -> 1000): 217.334732 ms
Parameters(records -> 10000): 4294.583544 ms
     */
    measure method "select with index" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38))")
        c.createStatement().execute("create table test_join(id numeric(38) primary key)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, PASSWORD)
        c.createStatement().execute("drop table test")
        c.createStatement().execute("drop table test_join")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, PASSWORD)

        val batchSize = 1000
        var currSize = 0
        val s = connection.prepareStatement(INSERT_TEST)
        val j = connection.prepareStatement(INSERT_TEST_JOIN)
        (0 to 10000).foreach(i => {
          s.setLong(1, i)
          s.setLong(2, i)
          j.setLong(1, i)
          s.addBatch()
          j.addBatch()

          currSize += 1
          if (currSize == batchSize) {
            s.executeBatch()
            j.executeBatch()
            currSize = 0
          }
        })

        s.executeBatch()
        j.executeBatch()
        s.close()
        j.close()
      } tearDown { _ =>
        connection.createStatement().execute("truncate table test")
        connection.createStatement().execute("truncate table test_join")
        connection.close()
      } config(
        exec.minWarmupRuns -> 1,
        exec.maxWarmupRuns -> 1,
        exec.benchRuns -> 1,
        exec.independentSamples -> 1
      ) in { records =>
        statement = connection.prepareStatement(SELECT_JOIN_TEST)

        (0 to records).foreach(i => {
          statement.setLong(1, i)
          val set = statement.executeQuery()
          set.next()
          val (id, value) = (set.getLong(1), set.getLong(2))
        })
      }
    }
  }
}
