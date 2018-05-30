package core.repository


import com.google.common.util.concurrent.ListenableFuture


trait RepositoryComponent[A, ID] {
  def repository: CrudRepository

  trait CrudRepository {
    def selectAsync(id: ID): ListenableFuture[A]

    def insertAsync(a: A): Unit

    def deleteAsync(id: ID): Unit

    def updateAsync(a: A): Unit

    def select(id: ID): A

    def insert(a: A): Unit

    def delete(id: ID): Unit

    def update(a: A): Unit
  }
}
