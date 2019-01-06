package akka.quill.sql.crud.web


import scala.concurrent.Future

trait Api[A, ID] {
  def findOne(id: ID): Future[Option[A]]

  def findAll(): Future[Seq[A]]

  def save(a: A): Future[A]

  def update(a: A): Future[A]

  def deleteOne(id: ID): Future[Long]

  def deleteAll(): Future[Long]
}
