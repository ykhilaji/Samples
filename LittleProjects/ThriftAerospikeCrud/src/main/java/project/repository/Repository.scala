package project.repository

trait Repository[F[_], A, ID] {
  def findOne(id: ID): F[Option[A]]

  def findAll(): F[Seq[A]]

  def save(a: A): F[A]

  def update(a: A): F[Unit]

  def remove(id: ID): F[Boolean]

  def removeAll(): F[Unit]
}
