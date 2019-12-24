package com.nryanov.calculator.http

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cats.~>
import cats.implicits._
import cats.effect.{Async, ContextShift, Effect, Sync}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe._
import sttp.tapir.server.akkahttp._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.openapi.OpenAPI
import sttp.model.StatusCode
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import io.circe.generic.auto._
import com.nryanov.calculator.mq.CommandClient
import com.nryanov.calculator.repository.Repository
import com.nryanov.calculator.model.{Expression, Result}
import com.nryanov.calculator.util.Metrics

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class HttpServer[F[_] : Sync : Effect](
                                        system: ActorSystem,
                                        repository: Repository[F],
                                        db: doobie.ConnectionIO ~> F, // this logic should be in separate service but for this sample it is ok to put it here
                                        commandClient: CommandClient[F]
                                      ) {

  import HttpServer._

  implicit private val actorSystem = system
  implicit private val dispatcher = system.dispatcher

  private val baseEndpoint: Endpoint[Unit, ErrorInfo, Unit, Nothing] = endpoint.errorOut(
    oneOf[ErrorInfo]( // <- explicit type is important
      statusMapping(StatusCode.BadRequest, jsonBody[BackEndError].description("backend error")),
      statusDefaultMapping(jsonBody[Unknown].description("unknown"))
    )
  )

  private val newExpressionCommandEndpoint: Endpoint[String, ErrorInfo, String, Nothing] =
    baseEndpoint
      .put
      .in("api" / "expression")
      .in(stringBody)
      .out(stringBody)
      .out(statusCode(StatusCode.Accepted))

  private val newExpressionCommandRoute = newExpressionCommandEndpoint.toRoute(expression => toFuture(commandClient.call(expression)))

  private val expressionByUuidEndpoint: Endpoint[String, ErrorInfo, Option[Expression], Nothing] =
    baseEndpoint
      .get
      .in("api" / "expression")
      .in(query[String]("uuid"))
      .out(jsonBody[Option[Expression]])

  private val expressionByUuidRoute = expressionByUuidEndpoint.toRoute(uuid => toFuture(db(repository.selectExpression(uuid))))

  private val resultByUuidEndpoint: Endpoint[String, ErrorInfo, Option[Result], Nothing] =
    baseEndpoint
      .get
      .in("api" / "result")
      .in(query[String]("uuid"))
      .out(jsonBody[Option[Result]])

  private val resultByUuidRoute = resultByUuidEndpoint.toRoute(uuid => toFuture(db(repository.selectExpressionResult(uuid))))

  private val expressionsByTimeEndpoint: Endpoint[(LocalDateTime, LocalDateTime), ErrorInfo, List[Expression], Nothing] =
    baseEndpoint
      .post
      .in("api" / "expression")
      .in(query[LocalDateTime]("from"))
      .in(query[LocalDateTime]("to"))
      .out(jsonBody[List[Expression]])

  private val expressionsByTimeRoute = expressionsByTimeEndpoint.toRoute { case (from, to) => toFuture(db(repository.selectExpressionsByTime(from, to))) }

  private val resultsByTimeEndpoint: Endpoint[(LocalDateTime, LocalDateTime), ErrorInfo, List[Result], Nothing] =
    baseEndpoint
      .post
      .in("api" / "result")
      .in(query[LocalDateTime]("from"))
      .in(query[LocalDateTime]("to"))
      .out(jsonBody[List[Result]])

  private val resultsByTimeRoute = resultsByTimeEndpoint.toRoute { case (from, to) => toFuture(db(repository.selectExpressionResultsByTime(from, to))) }

  private val metricsEndpoint: Endpoint[Unit, ErrorInfo, String, Nothing] =
    baseEndpoint
      .get
      .in("metrics")
      .out(stringBody)

  private val metricsRoute = metricsEndpoint.toRoute(_ => toFuture(Metrics.prometheusMetrics))

  private def toFuture[A](fa: F[A]): Future[Either[ErrorInfo, A]] =
    Effect[F]
      .toIO(fa.map(_.asRight[ErrorInfo]).handleError(e => Left(BackEndError(e.getLocalizedMessage))))
      .unsafeToFuture()

  private val openApiDocs: OpenAPI = List(
    newExpressionCommandEndpoint,
    expressionByUuidEndpoint,
    resultByUuidEndpoint,
    expressionsByTimeEndpoint,
    resultsByTimeEndpoint
  )
    .toOpenAPI("The tapir library", "1.0.0")

  val route = {
    import akka.http.scaladsl.server.Directives._
    expressionByUuidRoute ~
      resultByUuidRoute ~
      expressionsByTimeRoute ~
      resultsByTimeRoute ~
      newExpressionCommandRoute ~
      metricsRoute ~
      new SwaggerAkka(openApiDocs.toYaml).routes
  }
}

object HttpServer {
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  def decode(s: String): DecodeResult[LocalDateTime] = Try(LocalDateTime.parse(s, formatter)) match {
    case Success(v) => DecodeResult.Value(v)
    case Failure(f) => DecodeResult.Error(s, f)
  }

  def encode(time: LocalDateTime): String = time.toString

  implicit val localDateTimeCodec: PlainCodec[LocalDateTime] = Codec.stringPlainCodecUtf8
    .mapDecode(decode)(encode)

  def apply[F[_] : Sync : Effect](
                                   system: ActorSystem,
                                   repository: Repository[F],
                                   db: doobie.ConnectionIO ~> F,
                                   commandClient: CommandClient[F]
                                 ): HttpServer[F] =
    new HttpServer(system, repository, db, commandClient)

  def bind[F[_] : Async : ContextShift](
                                         system: ActorSystem,
                                         materalizer: ActorMaterializer,
                                         httpServer: HttpServer[F]
                                       ): F[Http.ServerBinding] = {
    implicit val actorSystem = system
    implicit val actorMaterializer = materalizer

    Async
      .fromFuture(Async[F].delay(Http().bindAndHandle(httpServer.route, "localhost", 8080)))
  }
}
