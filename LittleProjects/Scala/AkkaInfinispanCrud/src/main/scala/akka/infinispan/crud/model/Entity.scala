package akka.infinispan.crud.model

case class Entity(id: Long, value: String)

object Entity {
  def apply(id: Long, value: String): Entity = new Entity(id, value)
}
