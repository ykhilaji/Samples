package stream.filter.akka.streams

import java.util.Calendar
import java.util.concurrent.TimeUnit

import com.aerospike.client.{AerospikeClient, Bin, Host, Key}
import com.aerospike.client.policy.ClientPolicy
import com.dimafeng.testcontainers.{Container, ForAllTestContainer, GenericContainer}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.testcontainers.containers.BindMode

import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

class AerospikeCacheTest extends WordSpec with Matchers with ForAllTestContainer with BeforeAndAfterEach {
  override val container: GenericContainer = GenericContainer("aerospike/aerospike-server:latest",
    exposedPorts = Seq(3000),
    waitStrategy = TimeoutWaitStrategy(FiniteDuration(30, TimeUnit.SECONDS)),
    classpathResourceMapping = Seq(("aerospike.conf", "/opt/aerospike/etc/aerospike.conf", BindMode.READ_ONLY)),
    command = Seq("/usr/bin/asd", "--foreground", "--config-file", "/opt/aerospike/etc/aerospike.conf")
  )

  lazy val cfg =
    s"""
      |cache {
      |  host = "localhost:${container.mappedPort(3000)}"
      |  namespace = "namespace_test"
      |  setName = "entity"
      |}
    """.stripMargin

  lazy val config = ConfigFactory.parseString(cfg).getConfig("cache")
  lazy val client = {
    val hosts: Array[Host] = Host.parseHosts(config.getString("host"), container.mappedPort(3000))
    val policy = new ClientPolicy()
    new AerospikeClient(policy, hosts: _*)
  }
  lazy val namespace = config.getString("namespace")
  lazy val setName = config.getString("setName")

  lazy val cache = AerospikeCache(config)

  "Aerospike cache in sync mode" should {
    "return true" in {
      client.put(null, new Key(namespace, setName, "1"), new Bin("flag", true))
      assert(cache.isExist(Entity("1")))
    }

    "return false" in {
      assert(!cache.isExist(Entity("3")))
    }

    "return batch results" in {
      client.put(null, new Key(namespace, setName, "1"), new Bin("flag", true))

      val expected = Seq(true, false)
      val actual = cache.isExist(immutable.Seq(Entity("1"), Entity("2")))

      assertResult(expected)(actual)
    }
  }

  "Aerospike cache in async mode" should {
    import scala.concurrent.ExecutionContext.Implicits.global

    "return true" in {
      client.put(null, new Key(namespace, setName, "1"), new Bin("flag", true))
      val result = Await.result(cache.isExistAsync(Entity("1")), Duration(1, TimeUnit.SECONDS))
      assert(result)
    }

    "return false" in {
      val result = Await.result(cache.isExistAsync(Entity("3")), Duration(1, TimeUnit.SECONDS))
      assert(!result)
    }

    "return batch results" in {
      client.put(null, new Key(namespace, setName, "1"), new Bin("flag", true))

      val expected = Seq(true, false)
      val actual = Await.result(cache.isExistAsync(immutable.Seq(Entity("1"), Entity("2"))), Duration(1, TimeUnit.SECONDS))

      assertResult(expected)(actual)
    }
  }

  override protected def afterEach(): Unit = {
    client.truncate(null, namespace, setName, Calendar.getInstance())
  }
}
