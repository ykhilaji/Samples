import java.util.concurrent.{Executor, TimeUnit}

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.common.util.concurrent.ListenableFuture
import core.configuration.RichRoutes
import core.model.{Address, Contact, User}
import core.service.RichUserCassandraServiceComponent
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import spray.json.JsString
import spray.json._
import core.model.JsonWrapper._

import scala.collection.JavaConverters._


class HttpTest extends FunSuite with Matchers with ScalatestRouteTest with MockitoSugar with BeforeAndAfterEach {
  var service: RichUserCassandraServiceComponent = mock[RichUserCassandraServiceComponent]
  val rishOps = mock[RichUserCassandraServiceComponent#UserRichServiceOperations]
  Mockito.when(service.richServiceOperations).thenReturn(rishOps)

  val userList = List(
    new User("email1", "f1", "l1", 10, List(new Contact("000", "123")).asJava, new Address("c1", "ct1", "s1")),
    new User("email2", "f1", "l3", 12, List(new Contact("001", "321")).asJava, new Address("c1", "ct1", "s2")),
    new User("email3", "f2", "l2", 14, List(new Contact("010", "132")).asJava, new Address("c2", "ct1", "s1")),
    new User("email4", "f2", "l2", 16, List(new Contact("011", "213")).asJava, new Address("c2", "ct1", "s1")),
    new User("email5", "f3", "l3", 18, List(new Contact("100", "312")).asJava, new Address("c3", "ct1", "s1"))
  )

  val routes = new RichRoutes(service)
  val route: Route = routes.route

  override protected def beforeEach(): Unit = {
    Mockito.reset(rishOps)
  }

  test("api/users get all") {
    Mockito.when(rishOps.selectJson()).thenAnswer((_: InvocationOnMock) => userList.toJson)

    Get("/api/users") ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).selectJson()

      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")), ("result", userList.toJson))
    }
  }

  test("api/users get by email") {
    Mockito.when(rishOps.selectAsync("email1")).thenAnswer((_: InvocationOnMock) => new ListenableFuture[User] {
      override def addListener(listener: Runnable, executor: Executor): Unit = {}

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = true

      override def isCancelled: Boolean = false

      override def isDone: Boolean = true

      override def get(): User = userList.filter(u => u.getEmail == "email1").head

      override def get(timeout: Long, unit: TimeUnit): User = userList.filter(u => u.getEmail == "email1").head
    })

    Get("/api/users?email=email1") ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).selectAsync("email1")

      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")), ("result", userList.filter(u => u.getEmail == "email1").head.toJson))
    }
  }

  test("api/users get by email null") {
    Mockito.when(rishOps.selectAsync("email1")).thenAnswer((_: InvocationOnMock) => new ListenableFuture[User] {
      override def addListener(listener: Runnable, executor: Executor): Unit = {}

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = true

      override def isCancelled: Boolean = false

      override def isDone: Boolean = true

      override def get(): User = null

      override def get(timeout: Long, unit: TimeUnit): User = null
    })

    Get("/api/users?email=email1") ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).selectAsync("email1")

      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")), ("result", new User().toJson))
    }
  }


  test("api/users get by firstname") {
    Mockito.when(rishOps.selectByFirstNameJson("f1")).thenAnswer((_: InvocationOnMock) => userList.filter(u => u.getFirstName == "f1").toJson)

    Get("/api/users/f1") ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).selectByFirstNameJson("f1")

      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")), ("result", userList.filter(u => u.getFirstName == "f1").toJson))
    }
  }

  test("api/users post") {
    Post("/api/users", HttpEntity(ContentTypes.`application/json`, new User("email1", "firstName", "lastName", 15).toJson.toString)) ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).updateAsync(new User("email1", "firstName", "lastName", 15))

      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")))
    }
  }

  test("api/users put") {
    Put("/api/users", HttpEntity(ContentTypes.`application/json`, new User("email1", "firstName", "lastName", 15).toJson.toString)) ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).insertAsync(new User("email1", "firstName", "lastName", 15))

      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")))
    }
  }

  test("api/users delete") {
    Delete("/api/users", HttpEntity(ContentTypes.`application/json`, JsObject(("email", JsString("email"))).toString)) ~> route ~> check {
      Mockito.verify(service).richServiceOperations
      Mockito.verify(rishOps).deleteAsync("email")
      responseAs[JsValue] shouldEqual JsObject(("status", JsString("ok")))
    }
  }
}
