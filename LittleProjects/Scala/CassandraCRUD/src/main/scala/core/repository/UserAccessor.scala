package core.repository

import com.datastax.driver.core.{ResultSet, ResultSetFuture}
import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.{Accessor, Param, Query}
import com.google.common.util.concurrent.ListenableFuture
import core.model.User

@Accessor
trait UserAccessor {
  @Query("select * from users.user")
  def select(): ListenableFuture[Result[User]]
  @Query("select * from users.user where email in (:e)")
  def selectByEmails(@Param("e") emails: List[String]): ListenableFuture[Result[User]]
  @Query("select * from users.user where firstName = :f")
  def selectByFirstName(@Param("f") firstName: String): ListenableFuture[Result[User]]
  @Query("select json * from users.user")
  def selectJson(): ListenableFuture[ResultSet]
  @Query("select json * from users.user where email in (?)")
  def selectByEmailsJson(emails: List[String]): ListenableFuture[ResultSet]
  @Query("select json * from users.user where firstName = :f")
  def selectByFirstNameJson(@Param("f") firstName: String): ResultSetFuture
}
