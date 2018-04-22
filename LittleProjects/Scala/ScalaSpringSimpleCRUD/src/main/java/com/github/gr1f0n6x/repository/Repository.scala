package com.github.gr1f0n6x.repository

import scalikejdbc.AutoSession

trait Repository[T, ID] {
  implicit val session: AutoSession.type = AutoSession

  def select(id: ID): Option[T]

  def select(): Seq[T]

  def insert(entity: T): Unit

  def insert(entities: Seq[T]): Unit

  def delete(id: ID): Unit

  def delete(ids: Seq[ID]): Unit

  def update(entity: T): Unit

  def update(entities: Seq[T]): Unit
}
