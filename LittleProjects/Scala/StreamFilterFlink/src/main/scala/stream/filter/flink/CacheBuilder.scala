package stream.filter.flink

import com.typesafe.config.Config

trait CacheBuilder extends Serializable {
  def build(config: Config): Cache
}
