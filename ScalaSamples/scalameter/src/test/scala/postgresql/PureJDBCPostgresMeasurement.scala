package postgresql

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
object PureJDBCPostgresMeasurement extends Bench.LocalTime {
  val URL = "jdbc:postgresql://192.168.99.100:5432/postgres"
  val USER = "postgres"
  val DRIVER = "org.postgresql.Driver"
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
Parameters(records -> 1000): 773.779235 ms
Parameters(records -> 10000): 7767.951047 ms
     */
    measure method "ordered insert single records" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")
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
Parameters(records -> 1000, batchSize -> 10): 107.783282 ms
Parameters(records -> 1000, batchSize -> 100): 40.595084 ms
Parameters(records -> 1000, batchSize -> 1000): 35.620767 ms
Parameters(records -> 10000, batchSize -> 10): 1049.110717 ms
Parameters(records -> 10000, batchSize -> 100): 374.467604 ms
Parameters(records -> 10000, batchSize -> 1000): 327.877153 ms
     */
    measure method "ordered insert batch" in {
      using(gen) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")
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
Parameters(records -> 1000): 794.9663 ms
Parameters(records -> 10000): 7911.322139 ms
 */
    measure method "ordered insert single records - table with 2 indexes" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")
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
Parameters(records -> 1000, batchSize -> 10): 147.354321 ms
Parameters(records -> 1000, batchSize -> 100): 56.794649 ms
Parameters(records -> 1000, batchSize -> 1000): 52.181054 ms
Parameters(records -> 10000, batchSize -> 10): 1215.310701 ms
Parameters(records -> 10000, batchSize -> 100): 560.787466 ms
Parameters(records -> 10000, batchSize -> 1000): 503.018506 ms
     */
    measure method "ordered insert batch - table with 2 indexes" in {
      using(gen) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")
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
Parameters(records -> 1000): 781.889504 ms
Parameters(records -> 10000): 7840.374217 ms
 */
    measure method "not ordered insert single records - table with 2 indexes" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")
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
Parameters(records -> 1000, batchSize -> 10): 134.305088 ms
Parameters(records -> 1000, batchSize -> 100): 60.41476 ms
Parameters(records -> 1000, batchSize -> 1000): 57.815998 ms
Parameters(records -> 10000, batchSize -> 10): 1314.554681 ms
Parameters(records -> 10000, batchSize -> 100): 608.413647 ms
Parameters(records -> 10000, batchSize -> 1000): 567.341894 ms
     */
    measure method "not ordered insert batch - table with 2 indexes" in {
      using(gen) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38) unique)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")
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
Parameters(records -> 1000): 3025.419865 ms
Parameters(records -> 10000): 29145.484495 ms
 */
    measure method "select" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")

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
Parameters(records -> 1000): 155.866657 ms
Parameters(records -> 10000): 3756.38575 ms
     */
    measure method "select with index" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")

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
Parameters(records -> 1000): 7974.080996 ms
Parameters(records -> 10000): 107180.762215 ms
 */
    measure method "select" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38), value numeric(38))")
        c.createStatement().execute("create table test_join(id numeric(38))")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.createStatement().execute("drop table test_join")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")

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
Parameters(records -> 1000): 451.001635 ms
Parameters(records -> 10000): 4052.759888 ms
     */
    measure method "select with index" in {
      using(records) beforeTests {
        Class.forName(DRIVER)
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("create table test(id numeric(38) primary key, value numeric(38))")
        c.createStatement().execute("create table test_join(id numeric(38) primary key)")
        c.close()
      } afterTests {
        val c = DriverManager.getConnection(URL, USER, "")
        c.createStatement().execute("drop table test")
        c.createStatement().execute("drop table test_join")
        c.close()
      } setUp { _ =>
        connection = DriverManager.getConnection(URL, USER, "")

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
