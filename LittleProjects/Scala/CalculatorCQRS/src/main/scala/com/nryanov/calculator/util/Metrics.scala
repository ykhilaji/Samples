package com.nryanov.calculator.util

import java.io.StringWriter

import cats.effect.Sync
import cats.implicits._
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

object Metrics extends LazyLogging {
  // initialize default metrics
  DefaultExports.initialize()

  def prometheusMetrics[F[_]: Sync](): F[String] = for {
    _ <- Logger[F].info("Getting prometheus metrics")
    metrics <- Sync[F].delay(metrics())
    _ <- Logger[F].debug(s"Metrics: $metrics")
  } yield metrics

  private def metrics(): String = {
    val writer = new StringWriter()
    TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())

    writer.toString
  }
}
