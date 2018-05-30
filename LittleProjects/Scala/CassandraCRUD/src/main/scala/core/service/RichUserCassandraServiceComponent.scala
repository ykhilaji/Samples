package core.service

import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.ListenableFuture
import core.model.User
import core.repository.UserAccessor
import spray.json._
import core.model.JsonWrapper._

import scala.collection.JavaConverters._

trait RichUserCassandraServiceComponent extends RichUserServiceComponent {
  this: UserCassandraServiceComponent =>

  val userAccessor: UserAccessor

  override def richServiceOperations: RichServiceOperations = new UserRichServiceOperations

  class UserRichServiceOperations extends RichServiceOperations {
    override def select(): Seq[User] = userAccessor.select().get(1, TimeUnit.SECONDS).all().asScala

    override def selectByEmails(emails: List[String]): Seq[User] = userAccessor.selectByEmails(emails).get(1, TimeUnit.SECONDS).all().asScala

    override def selectByFirstName(firstName: String): Seq[User] = userAccessor.selectByFirstName(firstName).get(1, TimeUnit.SECONDS).all().asScala

    override def selectJson(): JsValue = userAccessor.selectJson().get(1, TimeUnit.SECONDS).all().asScala.toJson

    override def selectByEmailsJson(emails: List[String]): JsValue = userAccessor.selectByEmailsJson(emails).get(1, TimeUnit.SECONDS).all().asScala.toJson

    override def selectByFirstNameJson(firstName: String): JsValue = userAccessor.selectByFirstNameJson(firstName).get(1, TimeUnit.SECONDS).all().asScala.toJson

    override def select(email: String): User = service.select(email)

    override def insert(user: User): Unit = service.insert(user)

    override def delete(email: String): Unit = service.delete(email)

    override def update(user: User): Unit = service.update(user)

    override def selectAsync(email: String): ListenableFuture[User] = service.selectAsync(email)

    override def insertAsync(user: User): Unit = service.insertAsync(user)

    override def deleteAsync(email: String): Unit = service.deleteAsync(email)

    override def updateAsync(user: User): Unit = service.updateAsync(user)
  }



}
