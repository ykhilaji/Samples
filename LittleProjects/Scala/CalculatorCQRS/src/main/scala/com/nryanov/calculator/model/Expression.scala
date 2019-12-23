package com.nryanov.calculator.model

import java.time.LocalDateTime

case class Expression(expressionId: String, expression: String, creationTime: LocalDateTime)
