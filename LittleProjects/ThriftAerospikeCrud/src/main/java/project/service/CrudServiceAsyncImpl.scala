package project.service

import java.{lang, util}

import cats.effect.IO
import crud.{CrudService, Entity}
import org.apache.thrift.async.AsyncMethodCallback
import project.repository.Repository
import scala.collection.JavaConverters._

class CrudServiceAsyncImpl(repository: Repository[IO, Entity, Long]) extends CrudService.AsyncIface {
  override def findOne(id: Long, resultHandler: AsyncMethodCallback[util.List[Entity]]): Unit = repository
    .findOne(id)
    .attempt
    .unsafeRunSync() match {
    case Right(e) => resultHandler.onComplete(e.toList.asJava)
    case Left(err) => resultHandler.onError(new Exception(err))
  }

  override def findAll(resultHandler: AsyncMethodCallback[util.List[Entity]]): Unit = repository
    .findAll()
    .attempt
    .unsafeRunSync() match {
    case Right(e) => resultHandler.onComplete(e.toList.asJava)
    case Left(err) => resultHandler.onError(new Exception(err))
  }

  override def create(entity: Entity, resultHandler: AsyncMethodCallback[Entity]): Unit = repository
    .save(entity)
    .attempt
    .unsafeRunSync() match {
    case Right(e) => resultHandler.onComplete(e)
    case Left(err) => resultHandler.onError(new Exception(err))
  }

  override def update(entity: Entity, resultHandler: AsyncMethodCallback[Void]): Unit = repository
    .update(entity)
    .attempt
    .unsafeRunSync() match {
    case Right(_) => resultHandler.onComplete(Void.TYPE.newInstance())
    case Left(err) => resultHandler.onError(new Exception(err))
  }

  override def remove(id: Long, resultHandler: AsyncMethodCallback[lang.Boolean]): Unit = repository
    .remove(id)
    .attempt
    .unsafeRunSync() match {
    case Right(e) => resultHandler.onComplete(e)
    case Left(err) => resultHandler.onError(new Exception(err))
  }

  override def removeAll(resultHandler: AsyncMethodCallback[Void]): Unit = repository
    .removeAll()
    .attempt
    .unsafeRunSync() match {
    case Right(e) => resultHandler.onComplete(Void.TYPE.newInstance())
    case Left(err) => resultHandler.onError(new Exception(err))
  }
}

object CrudServiceAsyncImpl {
  def apply(repository: Repository[IO, Entity, Long]): CrudServiceAsyncImpl =
    new CrudServiceAsyncImpl(repository)
}
