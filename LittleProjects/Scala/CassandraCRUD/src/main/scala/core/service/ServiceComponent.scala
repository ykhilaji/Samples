package core.service

import com.google.common.util.concurrent.ListenableFuture


trait ServiceComponent[A, ID] {
  def service: ServiceOperations

  trait ServiceOperations {
    def select(id: ID): A

    def insert(a: A): Unit

    def delete(id: ID): Unit

    def update(a: A): Unit

    def selectAsync(id: ID): ListenableFuture[A]

    def insertAsync(a: A): Unit

    def deleteAsync(id: ID): Unit

    def updateAsync(a: A): Unit
  }

}
