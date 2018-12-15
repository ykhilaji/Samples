package akka.infinispan.crud.common

import org.apache.logging.log4j.{LogManager, Logger}
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation.{CacheEntryCreated, CacheEntryModified, CacheEntryRemoved, TransactionCompleted}
import org.infinispan.notifications.cachelistener.event.{CacheEntryCreatedEvent, CacheEntryModifiedEvent, CacheEntryRemovedEvent, TransactionCompletedEvent}

@Listener
class CacheEventListener {
  val logger: Logger = LogManager.getLogger("cache-event-listener")

  @CacheEntryCreated
  def observeAdd(event: CacheEntryCreatedEvent[String, String]): Unit = {
    if (event.isPre) return
    logger.info(s"Cache entry ${event.getKey} = ${event.getValue} added in cache ${event.getCache}")
  }

  @CacheEntryModified
  def observeUpdate(event: CacheEntryModifiedEvent[String, String]): Unit = {
    if (event.isPre) return
    logger.info(s"Cache entry ${event.getKey} = ${event.getValue} modified in cache ${event.getCache}")
  }

  @CacheEntryRemoved
  def observeRemove(event: CacheEntryRemovedEvent[String, String]): Unit = {
    if (event.isPre) return
    logger.info(s"Cache entry ${event.getKey} removed in cache ${event.getCache}")
  }

  @TransactionCompleted
  def observeTransaction(event: TransactionCompletedEvent[String, String]): Unit = {
    logger.info("Transaction completed")
  }
}
