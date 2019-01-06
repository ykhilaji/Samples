package akka.quill.sql.crud.web

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.pattern.ask
import akka.quill.sql.crud.actors._
import akka.quill.sql.crud.model.Entity
import akka.util.Timeout

import scala.concurrent.Future

class RestApi(executor: ActorRef) extends Api[Entity, Long] {
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  override def findOne(id: Long): Future[Option[Entity]] = (executor ? FindOne(id)).mapTo[Option[Entity]]

  override def findAll(): Future[Seq[Entity]] = (executor ? FindAll).mapTo[Seq[Entity]]

  override def save(a: Entity): Future[Entity] = (executor ? Save(a)).mapTo[Entity]

  override def update(a: Entity): Future[Entity] = (executor ? Update(a)).mapTo[Entity]

  override def deleteOne(id: Long): Future[Long] = (executor ? DeleteOne(id)).mapTo[Long]

  override def deleteAll(): Future[Long] = (executor ? DeleteAll).mapTo[Long]
}

object RestApi {
  def apply(executor: ActorRef): RestApi = new RestApi(executor)
}