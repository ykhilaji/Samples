package com.nryanov.calculator

import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger
import com.nryanov.calculator.service.ExpressionExecutor
import com.nryanov.calculator.utils.ConsoleUtils
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.nryanov.calculator.repository.Repository
import doobie.util.transactor


object CalculatorApp extends IOApp with LazyLogging {
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- Logger[IO].info("Starting calculator app")
    config <- loadConfig()
    db <- connectToDb(config)
    repository = Repository(db)
    expression <- readExpression()
    id <- repository.storeExpression(expression)
    result <- executeExpression(expression)
      .flatMap(r => repository.storeResult(id, r).map(_ => ExitCode.Success))
      .recoverWith {
        case e => repository.storeResultError(id, e).map(_ => ExitCode(2))
      }
    _ <- Logger[IO].info("Stopping calculator app")
  } yield result

  def loadConfig(): IO[Configuration] = for {
    _ <- Logger[IO].info("Load config")
    config <- Configuration.load[IO]
    _ <- Logger[IO].info(s"Configuration: $config")
  } yield config

  def readExpression(): IO[String] = for {
    _ <- ConsoleUtils.printStr[IO]("Write math expression")
    expression <- ConsoleUtils.readStr[IO]()
  } yield expression

  def executeExpression(expression: String): IO[Double] = ExpressionExecutor().execute[IO](expression)

  def connectToDb(configuration: Configuration): IO[Resource[IO, transactor.Transactor[IO]]] = for {
    _ <- Logger[IO].info("Connecting to database")
    db <- IO.delay(DataSource.transactor[IO](configuration))
  } yield db
}
