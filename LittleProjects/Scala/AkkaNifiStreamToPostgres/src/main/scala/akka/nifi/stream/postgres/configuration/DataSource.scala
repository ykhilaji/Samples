package akka.nifi.stream.postgres.configuration

import scalikejdbc.ConnectionPool

object DataSource {
  def init(): Unit = ConnectionPool.singleton(Configuration.dbUrl, Configuration.dbUser, Configuration.dbPass)
}