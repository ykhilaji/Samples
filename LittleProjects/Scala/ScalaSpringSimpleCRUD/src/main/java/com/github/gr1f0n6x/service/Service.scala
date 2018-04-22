package com.github.gr1f0n6x.service

trait Service[T, ID] {
  def select(id: ID): String

  def select(): String

  def insert(entity: String): String

  def delete(id: ID): String

  def update(entity: String): String
}
