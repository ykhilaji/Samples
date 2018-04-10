package service

import model.{Flux, Mono}

trait Service[T] {
  def select(id: Long): Mono[T]

  def selectAll(): Flux[T]

  def insert(entity: T): Mono[T]

  def update(entity: T): Mono[T]

  def delete(id: Long): Mono[T]
}
