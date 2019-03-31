package org.apache.spark.metrics.source

import com.codahale.metrics.{Counter, MetricRegistry}
import org.apache.spark.SparkEnv

class CacheMetric extends Source with Serializable {
  override def sourceName: String = "CacheMetrics"

  override def metricRegistry: MetricRegistry = new MetricRegistry

  val hits: Counter = metricRegistry.counter(MetricRegistry.name("hits"))

  val misses: Counter = metricRegistry.counter(MetricRegistry.name("misses"))
}

object CacheMetric {
  def apply(): CacheMetric = {
    val r = new CacheMetric()
    SparkEnv.get.metricsSystem.registerSource(r)
    r
  }
}