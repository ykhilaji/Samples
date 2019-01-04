package com.github.gr1f0n6x.scalatra.doobie.crud.service

trait Service[A, ID] {
  def findOne(id: ID): Either[Throwable, A]

  def findAll(): Either[Throwable, Seq[A]]

  def save(a: A): Either[Throwable, Any]

  def update(a: A): Either[Throwable, Any]

  def deleteOne(id: ID): Either[Throwable, Any]

  def deleteAll(): Either[Throwable, Any]
}
