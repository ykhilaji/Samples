package akka.nifi.stream.postgres.service

import akka.nifi.stream.postgres.repository.Repository

trait DefaultService[Entity, PK] {
  type A <: Repository[Entity, PK]
  val repository: A

  def findOne(pk: PK): Either[Throwable, Option[Entity]]

  def findAll(): Either[Throwable, Seq[Entity]]

  def delete(pk: PK): Either[Throwable, Unit]

  def save(e: Entity): Either[Throwable, Entity]

  def update(e: Entity): Either[Throwable, Entity]
}

object DefaultService {
  type Aux[E, P, C <: Repository[E, P]] = DefaultService[E, P] {type A = C}
}