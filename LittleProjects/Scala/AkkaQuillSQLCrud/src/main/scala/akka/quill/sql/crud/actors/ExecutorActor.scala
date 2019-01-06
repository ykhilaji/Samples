package akka.quill.sql.crud.actors

import akka.actor.Status.Failure
import akka.actor.{Actor, Props}
import akka.quill.sql.crud.model.Entity
import akka.quill.sql.crud.service.EntityService

class ExecutorActor extends Actor {
  val service = EntityService()

  override def receive: Receive = {
    case FindOne(id) => execute(service.findOne(id))
    case FindAll => execute(service.findAll())
    case Save(e) => execute(service.save(e))
    case Update(e) => execute(service.update(e))
    case DeleteOne(id) => execute(service.deleteOne(id))
    case DeleteAll => execute(service.deleteAll())
  }

  private def execute(f: => Either[Throwable, Any]): Unit = {
    f match {
      case Left(err) => sender() ! Failure(err)
      case Right(v) => sender() ! v
    }
  }
}

object ExecutorActor {
  def props(): Props = Props(new ExecutorActor())
}

trait ExecutorActorMessage

case class FindOne(id: Long) extends ExecutorActorMessage

case object FindAll extends ExecutorActorMessage

case class Save(e: Entity) extends ExecutorActorMessage

case class Update(e: Entity) extends ExecutorActorMessage

case class DeleteOne(id: Long) extends ExecutorActorMessage

case object DeleteAll extends ExecutorActorMessage