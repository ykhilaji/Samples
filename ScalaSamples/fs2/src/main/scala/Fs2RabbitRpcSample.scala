import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit

import fs2._
import cats.Monad
import cats.implicits._
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync, Timer}
import dev.profunktor.fs2rabbit.config.{Fs2RabbitConfig, Fs2RabbitNodeConfig}
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.effects.MessageEncoder
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model
import dev.profunktor.fs2rabbit.model.{AMQPConnection, AmqpMessage, AmqpProperties, ExchangeName, ExchangeType, QueueName, RoutingKey}

import scala.concurrent.duration.FiniteDuration

object Fs2RabbitRpcSample extends IOApp {
  val RPC_EXCHANGE = "RPC_EXCHANGE"
  val EMPTY_EXCHANGE = "" // default exchange
  val RPC_QUEUE = "RPC_QUEUE"
  val REPLY_QUEUE = "REPLY_QUEUE"

  case class Common[F[_]](
                           client: RabbitClient[F],
                           connection: AMQPConnection
                         )

  case class Application[F[_]](
                                rpcClient: RpcClient[F],
                                rpcServer: RpcServer[F]
                              )

  implicit def encoder[F[_]](implicit F: Monad[F]): MessageEncoder[F, AmqpMessage[String]] =
    Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s => s.copy(payload = s.payload.getBytes(StandardCharsets.UTF_8)).pure[F])

  override def run(args: List[String]): IO[ExitCode] = common[IO]()
    .evalTap(common => init(common.client, common.connection))
    .flatMap(common => application(common))
    .use { application =>
      for {
        _ <- Stream(
          application.rpcServer.start(),
          Stream.eval(IO(scala.io.StdIn.readLine())).evalMap(input => application.rpcClient.call(input)).repeat
        ).parJoin(2).compile.drain
      } yield ExitCode.Success
    }

  private def application[F[_] : ConcurrentEffect : ContextShift : Timer](common: Common[F]): Resource[F, Application[F]] = for {
    // use function for creating consumers/publishers because of hardcoded values for queues/exchanges
    clientConsumer <- createClientConsumer(common.client, common.connection)
    clientPublisher <- createClientPublisher(common.client, common.connection)
    serverConsumer <- createServerConsumer(common.client, common.connection)
    serverPublisher <- createServerPublisher(common.client, common.connection)
  } yield Application(
    new RpcClient[F](clientConsumer, clientPublisher),
    new RpcServer[F](serverConsumer, serverPublisher)
  )

  private def common[F[_] : ConcurrentEffect : ContextShift](): Resource[F, Common[F]] = for {
    cfg <- config()
    blocker <- Blocker[F]
    client <- Resource.liftF(rabbitClient[F](cfg, blocker))
    connection <- client.createConnection
  } yield Common(client, connection)

  private def config[F[_] : Sync](): Resource[F, Fs2RabbitConfig] = Resource.pure(Fs2RabbitConfig(
    virtualHost = "/",
    nodes = NonEmptyList.one(
      Fs2RabbitNodeConfig(
        host = "192.168.99.100",
        port = 5672
      )
    ),
    username = Some("guest"),
    password = Some("guest"),
    ssl = false,
    connectionTimeout = 3,
    requeueOnNack = false,
    internalQueueSize = Some(500),
    automaticRecovery = true
  ))

  private def rabbitClient[F[_] : ConcurrentEffect : ContextShift](config: Fs2RabbitConfig, blocker: Blocker): F[RabbitClient[F]] =
    RabbitClient[F](config, blocker)

  private def init[F[_] : ConcurrentEffect : ContextShift](rabbitClient: RabbitClient[F], connection: AMQPConnection): F[Unit] =
    rabbitClient.createChannel(connection).use { implicit channel =>
      ConcurrentEffect[F].delay(println("Creating exchange")) *>
        rabbitClient.declareExchange(ExchangeName(RPC_EXCHANGE), ExchangeType.Direct) *>
        ConcurrentEffect[F].delay(println("Creating queue")) *>
        rabbitClient.declareQueue(DeclarationQueueConfig.default(QueueName(RPC_QUEUE))) *>
        rabbitClient.declareQueue(DeclarationQueueConfig.default(QueueName(REPLY_QUEUE))) *>
        ConcurrentEffect[F].delay(println("Binding exchange and queue")) *>
        rabbitClient.bindQueue(QueueName(RPC_QUEUE), ExchangeName(RPC_EXCHANGE), RoutingKey(RPC_QUEUE))
    }

  private def createClientConsumer[F[_] : ConcurrentEffect : ContextShift](rabbitClient: RabbitClient[F], connection: AMQPConnection): Resource[F, Stream[F, model.AmqpEnvelope[String]]] =
    rabbitClient.createChannel(connection)
      .evalMap { implicit channel =>
        ConcurrentEffect[F].delay(println("Creating client consumer")) *>
          rabbitClient.createAutoAckConsumer[String](QueueName(REPLY_QUEUE))
      }

  private def createClientPublisher[F[_] : ConcurrentEffect : ContextShift](
                                                                             rabbitClient: RabbitClient[F],
                                                                             connection: AMQPConnection
                                                                           )(implicit encoder: MessageEncoder[F, AmqpMessage[String]]): Resource[F, AmqpMessage[String] => F[Unit]] =
    rabbitClient.createChannel(connection)
      .evalMap { implicit channel =>
        ConcurrentEffect[F].delay(println("Creating client publisher")) *>
          rabbitClient.createPublisher[AmqpMessage[String]](ExchangeName(RPC_EXCHANGE), RoutingKey(RPC_QUEUE))
      }

  private def createServerConsumer[F[_] : ConcurrentEffect : ContextShift](rabbitClient: RabbitClient[F], connection: AMQPConnection): Resource[F, Stream[F, model.AmqpEnvelope[String]]] =
    rabbitClient.createChannel(connection)
      .evalMap { implicit channel =>
        ConcurrentEffect[F].delay(println("Creating server consumer")) *>
          rabbitClient.createAutoAckConsumer[String](QueueName(RPC_QUEUE))
      }

  private def createServerPublisher[F[_] : ConcurrentEffect : ContextShift](
                                                                             rabbitClient: RabbitClient[F],
                                                                             connection: AMQPConnection
                                                                           )(implicit encoder: MessageEncoder[F, AmqpMessage[String]]): Resource[F, AmqpMessage[String] => F[Unit]] =
    rabbitClient.createChannel(connection)
      .evalMap { implicit channel =>
        ConcurrentEffect[F].delay(println("Creating server publisher")) *>
          rabbitClient.createPublisher[AmqpMessage[String]](ExchangeName(EMPTY_EXCHANGE), RoutingKey(REPLY_QUEUE))
      }

  class RpcClient[F[_] : Concurrent : Timer](
                                              consumer: Stream[F, model.AmqpEnvelope[String]],
                                              publisher: AmqpMessage[String] => F[Unit]
                                            )(implicit F: Sync[F]) {
    def call(str: String): F[model.AmqpEnvelope[String]] = {
      def go(): Stream[F, model.AmqpEnvelope[String]] = for {
        uuid <- Stream.eval(F.pure(UUID.randomUUID().toString))
        message = AmqpMessage[String](str, AmqpProperties(correlationId = uuid.some))
        _ <- Stream.eval(publisher(message))
        reply <- consumer.filter(_.properties.correlationId.contains(uuid)).take(1)
        _ <- Stream.eval(F.delay(println(s"Reply: ${reply.payload}")))
      } yield reply

      Concurrent.timeout(go().compile.lastOrError, FiniteDuration(100, TimeUnit.MILLISECONDS))
    }
  }

  class RpcServer[F[_]](
                         consumer: Stream[F, model.AmqpEnvelope[String]],
                         publisher: AmqpMessage[String] => F[Unit]
                       )(implicit F: Sync[F]) {
    def start(): Stream[F, Unit] = consumer.evalMap { envelope =>
      for {
        correlationId <- envelope.properties.correlationId.toRight(new IllegalArgumentException("Correlation id is empty")).liftTo[F]
        _ <- F.delay(println(s"Message: ${envelope.payload}"))
        reply = AmqpMessage[String](s"Reply: ${envelope.payload}", AmqpProperties(correlationId = correlationId.some))
        _ <- publisher(reply)
      } yield ()
    }
  }

}
