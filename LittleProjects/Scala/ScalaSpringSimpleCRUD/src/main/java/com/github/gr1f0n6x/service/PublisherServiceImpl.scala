package com.github.gr1f0n6x.service

import com.github.gr1f0n6x.model.Publisher
import com.github.gr1f0n6x.repository.PublisherRepository
import org.springframework.beans.factory.annotation.Autowired
import com.github.gr1f0n6x.model.JsonWrapper._
import spray.json._

@org.springframework.stereotype.Service
class PublisherServiceImpl extends PublisherService {
  @Autowired
  var repository: PublisherRepository = _

  override def select(id: Long) = repository.select(id).toJson.toString

  override def select() = repository.select().toJson.toString

  override def insert(entity: String) = repository.insert(entity.parseJson.convertTo[Publisher]).toJson.toString

  override def delete(id: Long) = repository.delete(id).toJson.toString

  override def update(entity: String) = repository.update(entity.parseJson.convertTo[Publisher]).toJson.toString
}
