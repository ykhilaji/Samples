package akka.mongo.crud.configuration

import akka.mongo.crud.model.{Event, EventSource}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.{MongoClient, MongoDatabase}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

object DataSource {
  val mongoClient: MongoClient = MongoClient(s"mongodb://${Configuration.host}:${Configuration.port}")
  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[Event], classOf[EventSource]), DEFAULT_CODEC_REGISTRY)
  val db: MongoDatabase = mongoClient.getDatabase("applicationDb").withCodecRegistry(codecRegistry)
}
