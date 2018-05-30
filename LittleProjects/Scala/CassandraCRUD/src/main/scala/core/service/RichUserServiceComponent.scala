package core.service

import core.model.User
import spray.json.JsValue

trait RichUserServiceComponent extends ServiceComponent[User, String] {
  def richServiceOperations: RichServiceOperations

  trait RichServiceOperations extends ServiceOperations {
    def select(): Seq[User]

    def selectByEmails(emails: List[String]): Seq[User]

    def selectByFirstName(firstName: String): Seq[User]

    def selectJson(): JsValue

    def selectByEmailsJson(emails: List[String]): JsValue

    def selectByFirstNameJson(firstName: String): JsValue
  }

}
