package akka.quill.sql.crud.service

trait Service[A, ID] {
  def findOne(id: ID): Either[Throwable, Option[A]]

  def findAll(): Either[Throwable, Seq[A]]

  def save(a: A): Either[Throwable, A]

  def update(a: A): Either[Throwable, A]

  def deleteOne(id: ID): Either[Throwable, Long]

  def deleteAll(): Either[Throwable, Long]
}
