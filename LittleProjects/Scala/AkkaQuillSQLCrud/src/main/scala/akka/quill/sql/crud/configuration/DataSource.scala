package akka.quill.sql.crud.configuration

import io.getquill.{PostgresJdbcContext, SnakeCase}

object DataSource {
  lazy val ctx = new PostgresJdbcContext(SnakeCase, "repository")
}
