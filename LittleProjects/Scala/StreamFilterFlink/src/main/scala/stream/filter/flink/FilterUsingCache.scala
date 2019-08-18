package stream.filter.flink

import com.typesafe.config.Config
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

class FilterUsingCache(builder: CacheBuilder, config: Config) extends ProcessFunction[Seq[Array[Byte]], Seq[Array[Byte]]] {
  @transient
  private var cache: Cache = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    cache = builder.build(config)
  }

  override def close(): Unit = {
    super.close()
    cache.close()
  }

  override def processElement(value: Seq[Array[Byte]], ctx: ProcessFunction[Seq[Array[Byte]], Seq[Array[Byte]]]#Context, out: Collector[Seq[Array[Byte]]]): Unit =
    out.collect(cache.exists(value).zip(value).filter(_._1).map(_._2))
}

object FilterUsingCache {
  def apply(builder: CacheBuilder, config: Config): FilterUsingCache = new FilterUsingCache(builder, config)
}