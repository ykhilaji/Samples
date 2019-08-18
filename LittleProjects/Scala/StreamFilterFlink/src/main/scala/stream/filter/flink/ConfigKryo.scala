package stream.filter.flink

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import com.typesafe.config.{Config, ConfigFactory}

class ConfigKryo extends Serializer[Config] {
  override def write(kryo: Kryo, output: Output, cfg: Config): Unit = {
    kryo.writeObject(output, cfg.entrySet())
  }

  override def read(kryo: Kryo, input: Input, cfgType: Class[Config]): Config = {
    val map = kryo.readObject(input, classOf[java.util.Map[String, AnyRef]])
    ConfigFactory.parseMap(map)
  }
}
