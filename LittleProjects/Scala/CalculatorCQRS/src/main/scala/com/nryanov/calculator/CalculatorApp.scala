package com.nryanov.calculator

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.implicits._
import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Effect, ExitCode, IO, IOApp, Resource, Sync}
import cats.~>
import com.nryanov.calculator.configuration.Configuration
import com.nryanov.calculator.db.{DataSource, FlywayMigrationTool}
import com.nryanov.calculator.http.HttpServer
import com.nryanov.calculator.logic.ExpressionExecutor
import com.nryanov.calculator.mq.{CommandClient, CommandServer, RabbitMQ}
import com.nryanov.calculator.repository.Repository
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.AMQPConnection
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger
import brave.Tracing
import zipkin2.Span
import zipkin2.reporter.{AsyncReporter, Sender}
import zipkin2.reporter.okhttp3.OkHttpSender

object CalculatorApp extends IOApp with LazyLogging {

  case class Common[F[_]](
                           cfg: Configuration,
                           actorSystem: ActorSystem,
                           actorMaterializer: ActorMaterializer,
                           rabbitClient: RabbitClient[F],
                           connection: AMQPConnection,
                           db: doobie.ConnectionIO ~> F,
                           frontTracing: Tracing,
                           backTracing: Tracing
                         )

  case class Application[F[_]](
                                httpServer: HttpServer[F],
                                commandServer: CommandServer[F],
                              )

  override def run(args: List[String]): IO[ExitCode] = common[IO]
    .flatMap(common => application[IO](common).map(app => (common, app)))
    .use { case (common, app) =>
      for {
        _ <- FlywayMigrationTool.migrate[IO](common.cfg)
        _ <- HttpServer.bind[IO](common.actorSystem, common.actorMaterializer, app.httpServer)
        _ <- app.commandServer.serve().compile.drain
      } yield ExitCode.Success
    }

  private def application[F[_] : ConcurrentEffect : ContextShift : Effect](common: Common[F]): Resource[F, Application[F]] = for {
    commandClient <- CommandClient[F](common.cfg, common.rabbitClient, common.connection, common.frontTracing)
    expressionExecutor = ExpressionExecutor()
    repository = Repository[F]()
    server = HttpServer(common.actorSystem, repository, common.db, commandClient, common.frontTracing)
    commandServer <- CommandServer[F](common.cfg, common.rabbitClient, common.connection, repository, expressionExecutor, common.db, common.backTracing)
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
    sender <- zipkinSender
    reporter <- zipkinReporter(sender)
    frontTracing <- zipkinTracer("front", reporter)
    backTracing <- zipkinTracer("back", reporter)
  } yield Common[F](cfg, system, materializer, rabbitClient, connection, db, frontTracing, backTracing)

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

  private def zipkinSender[F[_]](implicit F: Sync[F]): Resource[F, Sender] = {
    def acquire: F[Sender] = F.delay(OkHttpSender.create("http://192.168.99.100:9411/api/v2/spans")).widen[Sender]
    def release(sender: Sender): F[Unit] = F.delay(sender.close())

    Resource.make(acquire)(release)
  }

  private def zipkinReporter[F[_]](sender: Sender)(implicit F: Sync[F]): Resource[F, AsyncReporter[Span]] = {
    def acquire: F[AsyncReporter[Span]] = F.delay(AsyncReporter.create(sender))
    def release(reporter: AsyncReporter[Span]): F[Unit] = F.delay(reporter.close())

    Resource.make(acquire)(release)
  }

  private def zipkinTracer[F[_]](serviceName: String, reporter: AsyncReporter[Span])(implicit F: Sync[F]): Resource[F, Tracing] = {
    def acquire: F[Tracing] = F.delay(Tracing.newBuilder()
      .localServiceName(serviceName)
      .spanReporter(reporter)
      .build()
    )
    def release(tracing: Tracing): F[Unit] = F.delay(tracing.close())

    Resource.make(acquire)(release)
  }
}
