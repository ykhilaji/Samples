package com.github.gr1f0n6x.scalatra.doobie.crud.authentication

import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentrySupport, ScentryConfig}
import org.scalatra.ScalatraBase
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}


class CrudBasicAuthStrategy(protected override val app: ScalatraBase, realm: String) extends BasicAuthStrategy[User](app, realm) {

  protected def validate(userName: String, password: String)
                        (implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    if (userName == "admin" && password == "admin") Some(User("admin"))
    else None
  }

  protected def getUserId(user: User)
                         (implicit request: HttpServletRequest, response: HttpServletResponse): String = user.id


}

trait AuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {
  self: ScalatraBase =>


  override protected def basicAuth()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    val baReq = new BasicAuthStrategy.BasicAuthRequest(request)

    if (!baReq.providesAuth) {
      response.setHeader("WWW-Authenticate", "Basic realm=\"%s\"" format realm)
      halt(401, "Unauthenticated")
    }

    if (!baReq.isBasicAuth) {
      halt(400, "Bad Request")
    }

    scentry.authenticate("Basic")
  }



  val realm = "auth sample"

  protected def fromSession: PartialFunction[String, User] = {
    case id: String => User(id)
  }

  protected def toSession: PartialFunction[User, String] = {
    case usr: User => usr.id
  }

  protected val scentryConfig: ScentryConfiguration = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]


  override protected def configureScentry(): Unit = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies(): Unit = {
    scentry.register("Basic", app => new CrudBasicAuthStrategy(app, realm))
  }

}

case class User(id: String)
