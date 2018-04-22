package com.github.gr1f0n6x.configuration

import javax.annotation.PostConstruct

import org.apache.log4j.Logger
import scalikejdbc._
import org.springframework.context.annotation.Configuration

@Configuration
class Source {
  val logger: Logger = Logger.getLogger(classOf[Source])

  @PostConstruct
  def dataSourceConfiguration(): Unit = {
    Class.forName("org.postgresql.Driver")

    val settings = ConnectionPoolSettings(
      initialSize = 1,
      maxSize = 5,
      connectionTimeoutMillis = 1000,
      validationQuery = "select 1"
    )

    GlobalSettings.queryCompletionListener = (query, params, count) => {
      logger.info(s"Query completed. $query, ${params mkString ","}, $count")
    }

    GlobalSettings.queryFailureListener = (query, params, error) => {
      logger.error(s"Query failed. $query, ${params mkString ","}, $error")
    }

    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = true,
      logLevel = 'INFO
    )

    ConnectionPool.singleton(
      url = "jdbc:postgresql://localhost:5432/postgres",
      user = "postgres",
      password = "",
      settings = settings)
  }
}
