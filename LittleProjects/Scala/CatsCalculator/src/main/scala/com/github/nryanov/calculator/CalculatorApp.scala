package com.github.nryanov.calculator

import cats.effect.{ExitCode, IO, IOApp}

object CalculatorApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- Utils.prompt[IO]("Put your math expression:")
    expression <- Utils.readExpression[IO]
    result <- ExpressionExecutor.execute[IO](expression)
      .flatMap(digit => Utils.prompt[IO](s"Result: $digit").map(_ => ExitCode.Success))
      .handleErrorWith { e => Utils.prompt[IO](e.getLocalizedMessage).map(_ => ExitCode(2)) }
  } yield result
}
