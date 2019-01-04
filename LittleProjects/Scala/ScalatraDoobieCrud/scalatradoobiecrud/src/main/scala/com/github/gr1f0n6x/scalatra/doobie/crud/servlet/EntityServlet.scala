package com.github.gr1f0n6x.scalatra.doobie.crud.servlet

import com.github.gr1f0n6x.scalatra.doobie.crud.authentication.AuthenticationSupport
import com.github.gr1f0n6x.scalatra.doobie.crud.model.{Entity, Error}
import com.github.gr1f0n6x.scalatra.doobie.crud.service.EntityService
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.scalatra._
import org.slf4j.LoggerFactory


class EntityServlet extends ScalatraServlet with JacksonJsonSupport with AuthenticationSupport with MethodOverride {
  val service = EntityService()
  val logger =  LoggerFactory.getLogger(getClass)

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
    logger.info(s"Request: ${request.getRequestURI}")
  }

  after() {

  }

  get("/:id?/?") {
    params.getAs[Long]("id") match {
      case Some(id) => getResult(service.findOne(id))
      case None => getResult(service.findAll())
    }
  }

  get("/five") {
    getResult(service.findFirst5())
  }

  post("/?") {
    getResult(service.update(parsedBody.extract[Entity]))
  }

  put("/?") {
    getResult(service.save(parsedBody.extract[Entity]))
  }

  delete("/:id", request.getRemoteUser == "admin") {
    val id: Long = params.getAsOrElse[Long]("id", halt(400))
    getResult(service.deleteOne(id))
  }

  delete("/all", request.getRemoteUser == "admin") {
    getResult(service.deleteAll())
  }

  private def getResult(e: Either[Throwable, Any]): Any = e match {
    case Left(err) =>
      logger.error(err.getLocalizedMessage)
      Error(err.getLocalizedMessage)
    case Right(v) =>
      logger.info(s"Result: $v")
      v
  }
}
