package project

import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSuite, Matchers}
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.Wait
import project.cache.AerospikeCache

import project.model.EventInfo


class AerospikeCacheTest extends FunSuite with Matchers with ForAllTestContainer {
  override val container = GenericContainer(
    dockerImage = "aerospike/aerospike-server:latest",
    exposedPorts = Seq(3000),
    command = Seq("asd", "--foreground", "--config-file", "/opt/aerospike/etc/aerospike.conf"),
    classpathResourceMapping = Seq(("aerospike.conf", "/opt/aerospike/etc/aerospike.conf", BindMode.READ_ONLY)),
    waitStrategy = Wait.defaultWaitStrategy()
  )

  test("get not existing state") {
    // temporary solution for 127.0.0.1 <port> is not yet fully initialized
    Thread.sleep(5000)
    val config = ConfigFactory.parseString(
      s"""
        |  aerospike {
        |    host = "localhost",
        |    port = ${container.mappedPort(3000)},
        |    namespace = "test",
        |    setname = "eventInfo",
        |  }
      """.stripMargin)

    val cache = AerospikeCache(config.getConfig("aerospike"))
    assert(cache.get(1).isEmpty)
  }

  test("get existing state") {
    // temporary solution for 127.0.0.1 <port> is not yet fully initialized
    Thread.sleep(5000)
    val config = ConfigFactory.parseString(
      s"""
         |  aerospike {
         |    host = "localhost",
         |    port = ${container.mappedPort(3000)},
         |    namespace = "test",
         |    setname = "eventInfo",
         |  }
      """.stripMargin)

    val cache = AerospikeCache(config.getConfig("aerospike"))

    val eventInfo = EventInfo(1, 1, 0, 0)
    cache.put(2, eventInfo)
    assertResult(eventInfo)(cache.get(2).get)
  }
}
