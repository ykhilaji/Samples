package stream.filter.akka.streams

import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class InMemoryCache(p: Entity => Boolean) extends Cache {
  import InMemoryCache._

  override def isExist(entity: Entity): Boolean = {
    logger.debug(s"Check existence of $entity")
    p.apply(entity)
  }

  override def isExist(entities: immutable.Seq[Entity]): immutable.Seq[Boolean] = {
    logger.debug(s"Check existence of $entities")
    entities.map(p(_))
  }

  override def isExistAsync(entity: Entity)(implicit ex: ExecutionContext): Future[Boolean] = Future{
    logger.debug(s"Check existence of $entity")
    p.apply(entity)
  }

  override def isExistAsync(entities: immutable.Seq[Entity])(implicit ex: ExecutionContext): Future[immutable.Seq[Boolean]] = Future{
    logger.debug(s"Check existence of $entities")
    entities.map(p(_))
  }
}

object InMemoryCache {
  val logger = LoggerFactory.getLogger(InMemoryCache.getClass)

  def apply(p: Entity => Boolean): InMemoryCache = new InMemoryCache(p)
}