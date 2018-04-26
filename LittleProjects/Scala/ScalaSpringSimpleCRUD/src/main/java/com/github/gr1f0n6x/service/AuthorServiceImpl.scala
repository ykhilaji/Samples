package com.github.gr1f0n6x.service

import com.github.gr1f0n6x.model.Author
import com.github.gr1f0n6x.repository.AuthorRepository
import org.springframework.beans.factory.annotation.Autowired
import com.github.gr1f0n6x.model.JsonWrapper._
import spray.json._

@org.springframework.stereotype.Service
class AuthorServiceImpl extends AuthorService {
  @Autowired
  var repository: AuthorRepository = _

  override def select(id: Long) = repository.select(id).toJson.toString

  override def select() = repository.select().toJson.toString

  override def insert(entity: String) = repository.insert(entity.parseJson.convertTo[Author]).toJson.toString

  override def delete(id: Long) = repository.delete(id).toJson.toString

  override def update(entity: String) = repository.update(entity.parseJson.convertTo[Author]).toJson.toString
}
