package com.nryanov.calculator.utils

import cats.effect.Effect

object ConsoleUtils {
  def printStr[F[_]: Effect](str: String): F[Unit] = Effect[F].delay(println(str))

  def readStr[F[_]: Effect](): F[String] = Effect[F].delay(scala.io.StdIn.readLine())
}
