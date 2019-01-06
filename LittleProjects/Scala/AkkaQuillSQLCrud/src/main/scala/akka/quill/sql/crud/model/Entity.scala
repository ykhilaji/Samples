package akka.quill.sql.crud.model

import java.time.LocalDateTime

case class Entity(id: Long = 0, value: String, createTime: LocalDateTime = LocalDateTime.now())
