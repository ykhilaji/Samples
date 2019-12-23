package com.nryanov.calculator.http

import scala.util.control.NoStackTrace

sealed trait ErrorInfo

case class BackEndError(e: String) extends RuntimeException(e) with ErrorInfo with NoStackTrace

case class Unknown(code: Int, msg: String) extends ErrorInfo

case object NoContent extends ErrorInfo
