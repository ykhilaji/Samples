package core.service


import com.google.common.util.concurrent.ListenableFuture
import core.model.User
import core.repository.UserCassandraRepositoryComponent

trait UserCassandraServiceComponent extends ServiceComponent[User, String] {
  this: UserCassandraRepositoryComponent =>

  override def service: ServiceOperations = new UserService

  class UserService extends ServiceOperations {
    override def select(email: String): User = repository.select(email)

    override def insert(user: User): Unit = repository.insert(user)

    override def delete(email: String): Unit = repository.delete(email)

    override def update(user: User): Unit = repository.update(user)

    override def selectAsync(email: String): ListenableFuture[User] = repository.selectAsync(email)

    override def insertAsync(user: User): Unit = repository.insertAsync(user)

    override def deleteAsync(email: String): Unit = repository.deleteAsync(email)

    override def updateAsync(user: User): Unit = repository.updateAsync(user)
  }
}
