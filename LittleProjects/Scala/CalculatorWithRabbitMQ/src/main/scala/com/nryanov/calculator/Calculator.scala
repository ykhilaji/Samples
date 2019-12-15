package com.nryanov.calculator


import java.util.concurrent.Executors

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import com.nryanov.calculator.config.Configuration
import com.nryanov.calculator.datasource.DataSource
import com.nryanov.calculator.logic.ExpressionExecutor
import com.nryanov.calculator.mq.{RabbitMqFlow, RabbitMqRpcClient, RabbitMqRpcServer}
import com.nryanov.calculator.repository.ExpressionRepository
import com.nryanov.calculator.utils.ConsoleUtils
import com.zaxxer.hikari.HikariConfig
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model
import fs2.Stream
import doobie.util.transactor

object Calculator extends IOApp {

  private final case class ApplicationConfiguration(configuration: Configuration, hikariConfig: HikariConfig, rabbitConfig: Fs2RabbitConfig)

  private final case class Application[F[_]](
                                              applicationConfiguration: ApplicationConfiguration,
                                              rabbitClient: RabbitClient[F],
                                              expressionExecutor: ExpressionExecutor,
                                              repository: ExpressionRepository[F]
                                            )

  override def run(args: List[String]): IO[ExitCode] = createApplication[IO]().use { app =>
    implicit val rabbitClient = app.rabbitClient

    runRpcServer[IO](app.applicationConfiguration.configuration, app.expressionExecutor, app.repository)
      .concurrently(runRpcClient[IO](app.applicationConfiguration.configuration))
      .compile.drain.as(ExitCode.Success)
  }

  private def createApplication[F[_] : ConcurrentEffect : ContextShift](): Resource[F, Application[F]] = for {
    appCfg <- readCfg[F]()
    transactor <- createTransactor[F](appCfg)
    rabbitClient <- createRabbitMQClient[F](appCfg)
    expressionExecutor = ExpressionExecutor()
    repository = ExpressionRepository[F](transactor)
  } yield Application(appCfg, rabbitClient, expressionExecutor, repository)

  private def runRpcServer[F[_] : Sync](configuration: Configuration, expressionExecutor: ExpressionExecutor, repository: ExpressionRepository[F])(implicit rabbitClient: RabbitClient[F]): Stream[F, model.AmqpEnvelope[String]] =
    Stream.resource(rabbitClient.createConnectionChannel).flatMap { implicit channel =>
      RabbitMqRpcServer(configuration, expressionExecutor, repository).serve()
    }

  private def runRpcClient[F[_] : ConcurrentEffect](configuration: Configuration)(implicit rabbitClient: RabbitClient[F]): Stream[F, model.AmqpEnvelope[String]] =
    Stream.resource(rabbitClient.createConnectionChannel).flatMap { implicit channel =>
      val client = RabbitMqRpcClient(configuration)

      readExpression()
        .flatMap(expression => client.call(expression))
        .repeat
    }

  private def readExpression[F[_] : Sync](): Stream[F, String] = for {
    _ <- Stream.eval(ConsoleUtils.putStr("Write expression"))
    expression <- Stream.eval(ConsoleUtils.readStr())
    _ <- Stream.eval(ConsoleUtils.putStr(s"Expression: $expression"))
  } yield expression

  private def readCfg[F[_] : Sync](): Resource[F, ApplicationConfiguration] = for {
    configuration <- Configuration.load[F]
    datasourceCfg <- DataSource.createConfig[F](configuration)
    rabbitCfg <- RabbitMqFlow.rabbitConfig[F](configuration)
  } yield ApplicationConfiguration(configuration, datasourceCfg, rabbitCfg)

  private def createTransactor[F[_] : Async : ContextShift](applicationConfiguration: ApplicationConfiguration): Resource[F, transactor.Transactor[F]] =
    DataSource.create[F](applicationConfiguration.hikariConfig)

  private def createRabbitMQClient[F[_] : ConcurrentEffect : ContextShift](applicationConfiguration: ApplicationConfiguration): Resource[F, RabbitClient[F]] = for {
    blocker <- Resource.make(ConcurrentEffect[F].delay(Executors.newCachedThreadPool()))(es => ConcurrentEffect[F].delay(es.shutdown()))
      .map(Blocker.liftExecutorService)
    client <- RabbitMqFlow.create[F](applicationConfiguration.rabbitConfig, blocker).evalTap(client => RabbitMqFlow.init(applicationConfiguration.configuration, client))
  } yield client
}
