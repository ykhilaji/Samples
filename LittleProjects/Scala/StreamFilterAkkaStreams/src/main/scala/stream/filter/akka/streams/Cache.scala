package stream.filter.akka.streams

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

trait Cache {
  def isExist(entity: Entity): Boolean

  def isExistAsync(entity: Entity)(implicit ex: ExecutionContext): Future[Boolean]

  def isExist(entities: immutable.Seq[Entity]): immutable.Seq[Boolean]

  def isExistAsync(entities: immutable.Seq[Entity])(implicit ex: ExecutionContext): Future[immutable.Seq[Boolean]]
}
