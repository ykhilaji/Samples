package com.nryanov.calculator.utils

import cats.effect.Sync

object ConsoleUtils {
  def readStr[F[_]: Sync](): F[String] = Sync[F].delay(scala.io.StdIn.readLine())

  def putStr[F[_]: Sync](str: String): F[Unit] = Sync[F].delay(println(str))
}
