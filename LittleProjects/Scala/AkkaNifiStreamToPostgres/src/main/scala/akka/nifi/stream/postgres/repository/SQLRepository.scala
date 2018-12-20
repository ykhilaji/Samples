package akka.nifi.stream.postgres.repository

import scalikejdbc.DBSession

trait SQLRepository[Entity, PK] extends Repository[Entity, PK] {
  override type Context = DBSession
}
