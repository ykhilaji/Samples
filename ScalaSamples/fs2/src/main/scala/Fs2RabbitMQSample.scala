import fs2._
import cats.data.NonEmptyList
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.config.{Fs2RabbitConfig, Fs2RabbitNodeConfig}
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model
import dev.profunktor.fs2rabbit.model.{AMQPConnection, ExchangeName, ExchangeType, QueueName, RoutingKey}


object Fs2RabbitMQSample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = createResources[IO]().use { case (client, connection, publisher, consumer) =>
    for {
      _ <- init(client, connection)
      stream = Stream(
        Stream.eval(IO(scala.io.StdIn.readLine())).evalMap(input => publisher(input)).repeat,
        consumer.evalMap(env => IO(println(s"Message: ${env.payload}")))
      ).parJoin(2).compile
      _ <- stream.drain
    } yield ExitCode.Success
  }

  private def createResources[F[_] : ConcurrentEffect : ContextShift](): Resource[F, (RabbitClient[F], AMQPConnection, String => F[Unit], Stream[F, model.AmqpEnvelope[String]])] =
    for {
      cfg <- config()
      blocker <- Blocker[F]
      client <- Resource.liftF(rabbitClient(cfg, blocker))
      connection <- client.createConnection
      publisher <- createPublisher(client, connection)
      consumer <- createConsumer(client, connection)
    } yield (client, connection, publisher, consumer)

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
        rabbitClient.declareExchange(ExchangeName("test_exchange"), ExchangeType.Direct) *>
        ConcurrentEffect[F].delay(println("Creating queue")) *>
        rabbitClient.declareQueue(DeclarationQueueConfig.default(QueueName("test_queue"))) *>
        ConcurrentEffect[F].delay(println("Binding exchange and queue")) *>
        rabbitClient.bindQueue(QueueName("test_queue"), ExchangeName("test_exchange"), RoutingKey("test_queue"))
    }

  private def createConsumer[F[_] : ConcurrentEffect : ContextShift](rabbitClient: RabbitClient[F], connection: AMQPConnection): Resource[F, Stream[F, model.AmqpEnvelope[String]]] =
    rabbitClient.createChannel(connection)
      .evalMap { implicit channel =>
        ConcurrentEffect[F].delay(println("Creating consumer")) *>
          rabbitClient.createAutoAckConsumer[String](QueueName("test_queue"))
      }

  private def createPublisher[F[_] : ConcurrentEffect : ContextShift](rabbitClient: RabbitClient[F], connection: AMQPConnection): Resource[F, String => F[Unit]] =
    rabbitClient.createChannel(connection)
      .evalMap { implicit channel =>
        ConcurrentEffect[F].delay(println("Creating publisher")) *>
          rabbitClient.createPublisher[String](ExchangeName("test_exchange"), RoutingKey("test_queue"))
      }
}