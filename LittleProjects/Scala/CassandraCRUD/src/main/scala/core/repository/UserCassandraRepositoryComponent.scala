package core.repository


import com.datastax.driver.mapping.Mapper
import com.google.common.util.concurrent.ListenableFuture
import core.model.User

trait UserCassandraRepositoryComponent extends RepositoryComponent[User, String] {
  val userMapper: Mapper[User]

  override def repository: CrudRepository = new UserCrud

  class UserCrud extends CrudRepository {
    override def select(email: String): User = userMapper.get(email)

    override def insert(user: User): Unit = userMapper.save(user)

    override def delete(email: String): Unit = userMapper.delete(email)

    override def update(user: User): Unit = userMapper.save(user)

    override def selectAsync(email: String): ListenableFuture[User] = userMapper.getAsync(email)

    override def insertAsync(user: User): Unit = userMapper.saveAsync(user)

    override def deleteAsync(email: String): Unit = userMapper.deleteAsync(email)

    override def updateAsync(user: User): Unit = userMapper.saveAsync(user)
  }
}
