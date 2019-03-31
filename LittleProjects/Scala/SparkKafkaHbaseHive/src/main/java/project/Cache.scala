package project

trait Cache extends Serializable {
  def get(key: Long): Option[Entity]

  def put(key: Long, entity: Entity): Unit

  def remove(key: Long): Unit
}
