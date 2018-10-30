package basic

import scalikejdbc.async._

object ScalikeJDBCAsync extends App {
  AsyncConnectionPool.singleton("jdbc:postgresql://192.168.99.100:5432/scalikejdbc", "sa", "sa")
}
