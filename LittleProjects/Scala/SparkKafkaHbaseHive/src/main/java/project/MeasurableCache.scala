package project

import org.apache.spark.metrics.source.CacheMetric

trait MeasurableCache extends Cache {
  @transient
  private lazy val metric = CacheMetric()

  abstract override def get(key: Long): Option[Entity] = super.get(key) match {
    case None =>
      metric.misses.inc()
      None
    case s @ Some(_) =>
      metric.hits.inc()
      s
  }
}
