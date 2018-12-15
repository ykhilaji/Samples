package akka.infinispan.crud.common

import java.util.concurrent.TimeUnit

import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.eviction.{EvictionStrategy, EvictionThreadPolicy, EvictionType}
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.transaction.{LockingMode, TransactionMode}
import org.infinispan.util.concurrent.IsolationLevel

object DataSource {
  lazy val cacheManager: DefaultCacheManager = {
    val cache = new DefaultCacheManager()

    cache.defineConfiguration("entity", new ConfigurationBuilder()
      .locking().isolationLevel(IsolationLevel.READ_COMMITTED)
      .transaction().autoCommit(false).transactionMode(TransactionMode.TRANSACTIONAL).lockingMode(LockingMode.OPTIMISTIC)
      .eviction().`type`(EvictionType.COUNT).strategy(EvictionStrategy.LRU).threadPolicy(EvictionThreadPolicy.DEFAULT).size(10) // allow to cache 10 records
      .expiration().enableReaper().wakeUpInterval(10, TimeUnit.SECONDS).lifespan(60, TimeUnit.SECONDS) // default lifespan. May be overwritten
      .build())

    cache
  }
}
