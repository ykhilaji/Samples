package stream.filter.akka.streams

import java.util.Calendar
import java.util.concurrent.TimeUnit

import com.aerospike.client.{AerospikeClient, Host}
import com.aerospike.client.policy.ClientPolicy
import com.dimafeng.testcontainers.{Container, ForAllTestContainer, GenericContainer}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.testcontainers.containers.BindMode

import scala.concurrent.duration.FiniteDuration

class AerospikeCacheTest extends WordSpec with Matchers with ForAllTestContainer with BeforeAndAfterEach {
  override val container: Container = GenericContainer("aerospike/aerospike-server:4.5.3.4",
    exposedPorts = Seq(3000),
    waitStrategy = TimeoutWaitStrategy(FiniteDuration(30, TimeUnit.SECONDS)),
    classpathResourceMapping = Seq(("aerospike.conf", "/opt/aerospike/etc/aerospike.conf", BindMode.READ_ONLY)),
    command = Seq("/usr/bin/asd", "--foreground", "--config-file", "/opt/aerospike/etc/aerospike.conf")
  )

  val config = ConfigFactory.load().getConfig("cache")
  val client = {
    val hosts: Array[Host] = Host.parseHosts(config.getString("host"), 3000)
    val policy = new ClientPolicy()
    new AerospikeClient(policy, hosts: _*)
  }
  val namespace = config.getString("namespace")
  val setName = config.getString("setName")



  "Aerospike cache in sync mode" should {
    "return true" in {

    }

    "return false" in {

    }

    "return filtered list" in {

    }

    "return empty list" in {

    }
  }

  "Aerospike cache in async mode" should {
    "return true" in {

    }

    "return false" in {

    }

    "return filtered list" in {

    }

    "return empty list" in {

    }
  }

  override protected def afterEach(): Unit = {
    client.truncate(null, namespace, setName, Calendar.getInstance())
  }
}
