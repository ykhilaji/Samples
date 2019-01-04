package com.github.gr1f0n6x.scalatra.doobie.crud.servlet

import org.scalatra._

class IndexServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

}
