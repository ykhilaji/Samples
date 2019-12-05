package com.github.nryanov.calculator

import cats.effect.Sync

object Utils {
  def readExpression[F[_] : Sync]: F[String] = Sync[F].delay(scala.io.StdIn.readLine)

  def prompt[F[_] : Sync](str: String): F[Unit] = Sync[F].delay(println(str))
}
