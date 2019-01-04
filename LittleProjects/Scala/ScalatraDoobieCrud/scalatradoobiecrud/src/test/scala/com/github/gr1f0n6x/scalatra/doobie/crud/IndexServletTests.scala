package com.github.gr1f0n6x.scalatra.doobie.crud

import com.github.gr1f0n6x.scalatra.doobie.crud.servlet.IndexServlet
import org.scalatra.test.scalatest._

class IndexServletTests extends ScalatraFunSuite {

  addServlet(classOf[IndexServlet], "/*")

  test("GET / on CrudServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
