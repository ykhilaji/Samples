package akka.infinispan.crud.repository

import java.util.stream.Collectors

import akka.infinispan.crud.common.DataSource
import akka.infinispan.crud.model.Entity
import cats.effect.IO
import javax.transaction.TransactionManager
import org.infinispan.Cache

import scala.collection.JavaConverters._

class EntityRepository extends Repository[Long, Entity] {
  val cache: Cache[Long, Entity] = DataSource.cacheManager.getCache("entity")

  override def findOne(id: Long): IO[Option[Entity]] = IO {
    cache.get(id) match {
      case null => None
      case value => Some(value)
    }
  }

  override def findAll(): IO[Seq[Entity]] = IO(cache.values().stream().collect(Collectors.toList[Entity]).asScala)

  override def delete(id: Long): IO[Unit] = IO(cache.remove(id))

  override def deleteAll(): IO[Unit] = IO(cache.entrySet().forEach(e => cache.remove(e.getKey)))

  override def save(a: Entity): IO[Entity] = IO(cache.put(a.id, a))

  override def update(a: Entity): IO[Entity] = IO(cache.replace(a.id, a))

  override def transaction: TransactionManager = cache.getAdvancedCache.getTransactionManager
}

object EntityRepository {
  def apply(): EntityRepository = new EntityRepository()
}