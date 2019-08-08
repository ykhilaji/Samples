package org.apache.spark.sql.structured

import java.util.concurrent._

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.execution.streaming.{LongOffset, Offset, Source}
import org.apache.spark.sql.sources.{DataSourceRegister, StreamSourceProvider}
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}

import scala.collection.JavaConverters._


class CustomSourceRegister extends StreamSourceProvider with DataSourceRegister {
  override def sourceSchema(sqlContext: SQLContext, schema: Option[StructType], providerName: String, parameters: Map[String, String]): (String, StructType) =
    (shortName(), CustomSource.schema)

  override def createSource(sqlContext: SQLContext, metadataPath: String, schema: Option[StructType], providerName: String, parameters: Map[String, String]): Source =
    new CustomSource(sqlContext)

  override def shortName(): String = "CUSTOM_SOURCE"
}

class CustomSource(sqlContext: SQLContext) extends Source {
  private var offset: Option[LongOffset] = None
  private val batch = new LinkedBlockingQueue[Int]()
  // circular array
  private val drainBuffer = new ArrayBlockingQueue[Int](1)
  private val executor = Executors.newScheduledThreadPool(1)

  executor.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      offset = offset.map(_ + 1).orElse(Some(LongOffset(1)))
      batch.put(ThreadLocalRandom.current().nextInt())
    }
  }, 0, 100, TimeUnit.MILLISECONDS)

  override def schema: StructType = CustomSource.schema

  override def getOffset: Option[Offset] = offset

  override def getBatch(start: Option[Offset], end: Offset): DataFrame = {
    val startOffset = start.flatMap(LongOffset.convert).getOrElse(LongOffset(1))
    val endOffset = LongOffset.convert(end).get

    val elements = endOffset.offset - startOffset.offset

    val rdd = sqlContext.sparkContext
      .parallelize(batch.asScala.slice(0, elements.toInt).toSeq)
      .map(i => InternalRow(i))

    // internalCreateDataFrame - private[sql]
    sqlContext.sparkSession.internalCreateDataFrame(rdd, schema, isStreaming = true)
  }

  override def commit(end: Offset): Unit =
    batch.drainTo(drainBuffer, (LongOffset.convert(end).get.offset - offset.getOrElse(LongOffset(0)).offset).toInt)

  override def stop(): Unit = executor.shutdown()
}

object CustomSource {
  lazy val schema: StructType = StructType(Seq(StructField("number", IntegerType)))
}