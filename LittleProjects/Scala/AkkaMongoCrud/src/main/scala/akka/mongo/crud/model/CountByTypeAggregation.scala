package akka.mongo.crud.model

case class CountByTypeAggregation(_id: String, total: Long)

object CountByTypeAggregation {
  def apply(_id: String, total: Long): CountByTypeAggregation = new CountByTypeAggregation(_id, total)
}
