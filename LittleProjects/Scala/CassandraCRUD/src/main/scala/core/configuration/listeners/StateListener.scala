package core.configuration.listeners

import com.datastax.driver.core.{Cluster, Host}
import org.slf4j.{Logger, LoggerFactory}

class StateListener extends Host.StateListener {
  private val logger: Logger = LoggerFactory.getLogger("StateListener")

  override def onUnregister(cluster: Cluster): Unit = {}

  override def onAdd(host: Host): Unit = logger.info(s"Host added: $host")

  override def onUp(host: Host): Unit = {}

  override def onRegister(cluster: Cluster): Unit = {}

  override def onDown(host: Host): Unit = {}

  override def onRemove(host: Host): Unit = logger.info(s"Host removed: $host")
}
