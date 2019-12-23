package com.nryanov.calculator.model

import java.time.LocalDateTime

case class Result(expressionId: String, result: Option[Double] = None, error: Option[String] = None, creationTime: LocalDateTime)
