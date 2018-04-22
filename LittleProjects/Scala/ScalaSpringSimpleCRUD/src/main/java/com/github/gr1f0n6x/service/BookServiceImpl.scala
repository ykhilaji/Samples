package com.github.gr1f0n6x.service

import com.github.gr1f0n6x.model.Book
import com.github.gr1f0n6x.repository.BookRepository
import org.springframework.beans.factory.annotation.Autowired
import com.github.gr1f0n6x.model.JsonWrapper._
import spray.json._

@org.springframework.stereotype.Service
class BookServiceImpl extends BookService {
  @Autowired
  var repository: BookRepository = _

  override def select(id: Long) = repository.select(id).toJson.toString

  override def select() = repository.select().toJson.toString

  override def insert(entity: String) = repository.insert(entity.parseJson.convertTo[Book]).toJson.toString

  override def delete(id: Long) = repository.delete(id).toJson.toString

  override def update(entity: String) = repository.update(entity.parseJson.convertTo[Book]).toJson.toString
}
