package repository

trait DAO[T] {
  def select(id: Long): T

  def selectAll(): Seq[T]

  def insert(entity: T): Unit

  def update(entity: T): Unit

  def delete(id: Long): Unit
}
