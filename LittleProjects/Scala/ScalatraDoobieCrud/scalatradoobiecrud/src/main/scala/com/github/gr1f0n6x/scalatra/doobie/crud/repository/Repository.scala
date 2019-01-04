package com.github.gr1f0n6x.scalatra.doobie.crud.repository

import cats.effect.IO

trait Repository[A, ID] {
  def findOne(id: ID): IO[A]

  def findAll(): IO[Seq[A]]

  def save(a: A): IO[_]

  def update(a: A): IO[_]

  def deleteOne(id: ID): IO[_]

  def deleteAll(): IO[_]
}
