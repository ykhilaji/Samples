package akka.infinispan.crud.common

import org.apache.logging.log4j.{LogManager, Logger}
import org.infinispan.notifications.Listener
import org.infinispan.notifications.cachelistener.annotation._
import org.infinispan.notifications.cachelistener.event._
import org.infinispan.notifications.cachemanagerlistener.annotation.{CacheStarted, CacheStopped}
import org.infinispan.notifications.cachemanagerlistener.event.{CacheStartedEvent, CacheStoppedEvent}

@Listener
class CacheEventListener {
  val logger: Logger = LogManager.getLogger("cache-event-listener")

  @CacheStarted
  def observeCacheStart(event: CacheStartedEvent): Unit = {
    logger.info("Cache started")
  }

  @CacheStopped
  def observeCacheStop(event: CacheStoppedEvent): Unit = {
    logger.info("Cache stopped")
  }

  @CacheEntryCreated
  def observeAdd(event: CacheEntryCreatedEvent[_, _]): Unit = {
    if (event.isPre) return
    logger.info(s"Cache entry ${event.getKey} = ${event.getValue} added in cache ${event.getCache}")
  }

  @CacheEntryModified
  def observeUpdate(event: CacheEntryModifiedEvent[_, _]): Unit = {
    if (event.isPre) return
    logger.info(s"Cache entry ${event.getKey} = ${event.getValue} modified in cache ${event.getCache}")
  }

  @CacheEntryRemoved
  def observeRemove(event: CacheEntryRemovedEvent[_, _]): Unit = {
    if (event.isPre) return
    logger.info(s"Cache entry ${event.getKey} removed in cache ${event.getCache}")
  }

  @TransactionCompleted
  def observeTransaction(event: TransactionCompletedEvent[_, _]): Unit = {
    logger.info("Transaction completed")
  }

  @CacheEntriesEvicted
  def observeEviction(event: CacheEntriesEvictedEvent[_, _]): Unit = {
    if (event.isPre) return
    logger.info(s"Evicted entries: ${event.getEntries.toString}")
  }
}

object CacheEventListener {
  def apply(): CacheEventListener = new CacheEventListener()
}
