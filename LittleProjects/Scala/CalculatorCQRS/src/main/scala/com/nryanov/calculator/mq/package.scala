package com.nryanov.calculator

import java.nio.charset.StandardCharsets

import cats.data.Kleisli
import cats.effect.Sync
import cats.syntax.applicative._
import dev.profunktor.fs2rabbit.effects.MessageEncoder
import dev.profunktor.fs2rabbit.model.AmqpMessage

package object mq {
  implicit def encoder[F[_] : Sync]: MessageEncoder[F, AmqpMessage[String]] =
    Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s => s.copy(payload = s.payload.getBytes(StandardCharsets.UTF_8)).pure[F])
}
