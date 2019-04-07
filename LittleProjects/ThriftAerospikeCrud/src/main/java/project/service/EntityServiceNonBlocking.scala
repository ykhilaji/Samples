package project.service

import java.{lang, util}

import crud.{CrudService, Entity}
import org.apache.logging.log4j.LogManager
import org.apache.thrift.async.AsyncMethodCallback

import scala.concurrent.{Future, Promise}
import scala.collection.JavaConverters._

/**
  * @param client - this client is async but does not allow to execute multiple methods in parallel.
  * So, this is just a non-blocking bridge between callback method and future with result
  */
class EntityServiceNonBlocking(client: CrudService.AsyncClient) extends Service[Future, Entity, Long] {
  private val logger = LogManager.getLogger(classOf[EntityServiceNonBlocking])

  override def findOne(id: Long): Future[Option[Entity]] = {
    logger.info(s"Find one by id: $id")
    val promise = Promise[Option[Entity]]

    client.findOne(id, new AsyncMethodCallback[util.List[Entity]] {
      override def onComplete(response: util.List[Entity]): Unit = {
        logger.info(s"Result: ${response.asScala.headOption}")
        promise.success(response.asScala.headOption)
      }

      override def onError(exception: Exception): Unit = {
        logger.error(exception.getLocalizedMessage)
        promise.failure(exception)
      }
    })

    promise.future
  }

  override def findAll(): Future[Seq[Entity]] = {
    logger.info("Find all")
    val promise = Promise[Seq[Entity]]

    client.findAll(new AsyncMethodCallback[util.List[Entity]] {
      override def onComplete(response: util.List[Entity]): Unit = {
        logger.info(s"Result: ${response.asScala}")
        promise.success(response.asScala)
      }

      override def onError(exception: Exception): Unit = {
        logger.error(exception.getLocalizedMessage)
        promise.failure(exception)
      }
    })

    promise.future
  }

  override def create(a: Entity): Future[Entity] = {
    logger.info(s"Create: $a")
    val promise = Promise[Entity]

    client.create(a, new AsyncMethodCallback[Entity] {
      override def onComplete(response: Entity): Unit = {
        logger.info(s"Result: $response")
        promise.success(response)
      }

      override def onError(exception: Exception): Unit = {
        logger.error(exception.getLocalizedMessage)
        promise.failure(exception)
      }
    })

    promise.future
  }

  override def update(a: Entity): Future[Unit] = {
    logger.info(s"Update: $a")
    val promise = Promise[Unit]

    client.update(a, new AsyncMethodCallback[Void] {
      override def onComplete(response: Void): Unit = {
        logger.info(s"Result success")
        promise.success()
      }

      override def onError(exception: Exception): Unit = {
        logger.error(exception.getLocalizedMessage)
        promise.failure(exception)
      }
    })

    promise.future
  }

  override def remove(id: Long): Future[Boolean] = {
    logger.info(s"Remove: $id")
    val promise = Promise[Boolean]

    client.remove(id, new AsyncMethodCallback[lang.Boolean] {
      override def onComplete(response: lang.Boolean): Unit = {
        logger.info(s"Result: $response")
        promise.success(response)
      }

      override def onError(exception: Exception): Unit = {
        logger.error(exception.getLocalizedMessage)
        promise.failure(exception)
      }
    })

    promise.future
  }

  override def removeAll(): Future[Unit] = {
    logger.info("Remove all")
    val promise = Promise[Unit]

    client.removeAll(new AsyncMethodCallback[Void] {
      override def onComplete(response: Void): Unit = {
        logger.info(s"Result success")
        promise.success()
      }

      override def onError(exception: Exception): Unit = {
        logger.error(exception.getLocalizedMessage)
        promise.failure(exception)
      }
    })

    promise.future
  }
}

object EntityServiceNonBlocking {
  def apply(client: CrudService.AsyncClient): EntityServiceNonBlocking = new EntityServiceNonBlocking(client)
}