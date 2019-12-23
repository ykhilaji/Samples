package com.nryanov.calculator

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.implicits._
import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Effect, ExitCode, IO, IOApp, Resource, Sync}
import cats.~>
import com.nryanov.calculator.configuration.Configuration
import com.nryanov.calculator.db.DataSource
import com.nryanov.calculator.http.HttpServer
import com.nryanov.calculator.logic.ExpressionExecutor
import com.nryanov.calculator.mq.{CommandClient, CommandServer, RabbitMQ}
import com.nryanov.calculator.repository.Repository
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.AMQPConnection
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

object CalculatorApp extends IOApp with LazyLogging {

  case class Common[F[_]](
                           cfg: Configuration,
                           actorSystem: ActorSystem,
                           actorMaterializer: ActorMaterializer,
                           rabbitClient: RabbitClient[F],
                           connection: AMQPConnection,
                           db: doobie.ConnectionIO ~> F
                         )

  case class Application[F[_]](
                                httpServer: HttpServer[F],
                                commandServer: CommandServer[F],
                              )

  override def run(args: List[String]): IO[ExitCode] = common[IO]
    .flatMap(common => application[IO](common).map(app => (common, app)))
    .use { case (common, app) =>
      for {
        _ <- HttpServer.bind(common.actorSystem, common.actorMaterializer, app.httpServer)
        _ <- app.commandServer.serve().compile.drain
      } yield ExitCode.Success
    }

  private def application[F[_] : ConcurrentEffect : ContextShift : Effect](common: Common[F]): Resource[F, Application[F]] = for {
    commandClient <- CommandClient[F](common.cfg, common.rabbitClient, common.connection)
    expressionExecutor = ExpressionExecutor()
    repository = Repository[F]()
    server = HttpServer(common.actorSystem, repository, common.db, commandClient)
    commandServer <- CommandServer[F](common.cfg, common.rabbitClient, common.connection, repository, expressionExecutor, common.db)
  } yield Application[F](server, commandServer)

  private def common[F[_] : ConcurrentEffect : ContextShift]: Resource[F, Common[F]] = for {
    cfg <- Resource.liftF(Configuration.load[F]())
    system <- actorSystem()
    materializer <- actorMaterializer(system)
    rabbitCfg <- RabbitMQ.config(cfg)
    blocker <- Blocker[F]
    rabbitClient <- RabbitMQ.client(rabbitCfg, blocker).evalTap(client => RabbitMQ.init(cfg, client))
    connection <- rabbitClient.createConnection
    db <- DataSource.resource(cfg)
  } yield Common[F](cfg, system, materializer, rabbitClient, connection, db)

  private def actorSystem[F[_] : Async : ContextShift](): Resource[F, ActorSystem] = {
    def acquire: F[ActorSystem] =
      Logger[F].info("Creating actor system") *>
        Async[F].delay(ActorSystem("cqrs")) <*
        Logger[F].info("Actor system was created")

    def release(actorSystem: ActorSystem): F[Unit] =
      Logger[F].info("Terminating actor system") *>
        Async.fromFuture(Async[F].delay(actorSystem.terminate())).void *>
        Logger[F].info("Actor system was terminated")

    Resource.make(acquire)(release)
  }

  private def actorMaterializer[F[_] : Async : ContextShift](actorSystem: ActorSystem): Resource[F, ActorMaterializer] = {
    def acquire: F[ActorMaterializer] = Async[F].delay(ActorMaterializer()(actorSystem))

    def release(materializer: ActorMaterializer): F[Unit] = Async[F].delay(materializer.shutdown())

    Resource.make(acquire)(release)
  }
}
