package com.github.gr1f0n6x.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonWrapper extends DefaultJsonProtocol {
  implicit val author2json: RootJsonFormat[Author] = jsonFormat3(Author.apply)
  implicit val book2json: RootJsonFormat[Book] = jsonFormat3(Book.apply)
  implicit val publisher2json: RootJsonFormat[Publisher] = jsonFormat2(Publisher.apply)
}
