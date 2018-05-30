package core.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.datastax.driver.core.Row
import spray.json._

import scala.collection.JavaConverters._
import scala.collection.mutable


object JsonWrapper extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object Contact2JsonFormat extends RootJsonFormat[Contact] {
    override def read(json: JsValue): Contact = {
      val code = fromField[Option[String]](json, "code")
      val number = fromField[Option[String]](json, "number")

      new Contact(code.getOrElse(""), number.getOrElse(""))
    }

    override def write(obj: Contact): JsValue = obj match {
      case null => JsObject(
        "code" -> JsString(""),
        "number" -> JsString(""))
      case contact => JsObject(
        "code" -> JsString(contact.getCode),
        "number" -> JsString(contact.getNumber))
    }
  }

  implicit object Address2JsonFormat extends RootJsonFormat[Address] {
    override def read(json: JsValue): Address = {
      val country = fromField[Option[String]](json, "country")
      val city = fromField[Option[String]](json, "city")
      val street = fromField[Option[String]](json, "street")

      new Address(country.getOrElse(""), city.getOrElse(""), street.getOrElse(""))
    }

    override def write(obj: Address): JsValue = obj match {
      case null => JsObject(
        "country" -> JsString(""),
        "city" -> JsString(""),
        "street" -> JsString(""))
      case address => JsObject(
        "country" -> JsString(address.getCountry),
        "city" -> JsString(address.getCity),
        "street" -> JsString(address.getStreet))
    }
  }

  implicit object User2JsonFormat extends RootJsonFormat[User] {
    override def read(value: JsValue) = {
      val email = fromField[Option[String]](value, "email")
      val firstName = fromField[Option[String]](value, "firstName")
      val lastName = fromField[Option[String]](value, "lastName")
      val age = fromField[Option[Int]](value, "age")
      val contacts = fromField[Option[List[Contact]]](value, "contacts")
      val address = fromField[Option[Address]](value, "address")

      new User(email.getOrElse(""),
        firstName.getOrElse(""),
        lastName.getOrElse(""),
        age.getOrElse(0),
        contacts.getOrElse(List()).asJava,
        address.orNull)
    }

    override def write(obj: User): JsValue = obj match {
      case null => JsObject(
        "email" -> JsString(""),
        "firstName" -> JsString(""),
        "lastName" -> JsString(""),
        "age" -> JsNumber(0),
        "contacts" -> JsArray.empty,
        "address" -> JsObject(("country", JsString("")), ("city", JsString("")), ("street", JsString("")))
      )
      case user => JsObject(
        "email" -> JsString(obj.getEmail),
        "firstName" -> JsString(obj.getFirstName),
        "lastName" -> JsString(obj.getLastName),
        "age" -> JsNumber(obj.getAge),
        "contacts" -> (obj.getContacts match {
          case null => JsArray.empty
          case contacts => contacts.asScala.toSeq.toJson
        }),
        "address" -> (obj.getAddress match {
          case null => JsObject.empty
          case address => address.toJson
        })
      )
    }
  }

  implicit object Row2JsonFormat extends RootJsonFormat[Row] {
    // No need
    override def read(json: JsValue): Row = null

    override def write(obj: Row): JsValue = {
      JsArray
        .apply(obj
          .getColumnDefinitions
          .asList()
          .asScala
          .map(coldef => JsString
            .apply(obj
              .getObject(coldef.getName).toString)): _*)
    }
  }

  implicit object BufferWithRows2Json extends RootJsonFormat[mutable.Buffer[Row]] {
    // No need
    override def read(json: JsValue): mutable.Buffer[Row] = null

    override def write(obj: mutable.Buffer[Row]): JsValue = {
      JsArray.apply(obj.map(x => x.toJson): _*)
    }
  }

  implicit object BufferWithContacts2Json extends RootJsonFormat[mutable.Buffer[Contact]] {
    // No need
    override def read(json: JsValue): mutable.Buffer[Contact] = null

    override def write(obj: mutable.Buffer[Contact]): JsValue = {
      JsArray.apply(obj.map(x => x.toJson): _*)
    }
  }

}
