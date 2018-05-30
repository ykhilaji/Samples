package core

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}
import akka.stream.ActorMaterializer
import com.datastax.driver.mapping.Mapper
import core.configuration.{AppConfiguration, RichRoutes}
import core.model.User
import core.repository.{UserAccessor, UserCassandraRepositoryComponent}
import core.service.{RichUserCassandraServiceComponent, UserCassandraServiceComponent}
import org.slf4j.LoggerFactory



object HttpEntryPoint extends App {
  val logger = LoggerFactory.getLogger("AkkaCassandraCRUD")
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val context = system.dispatcher

  val richRouter = new RichRoutes(new RichUserCassandraServiceComponent
    with UserCassandraServiceComponent
    with UserCassandraRepositoryComponent {
    override val userAccessor: UserAccessor = AppConfiguration.userAccessor
    override val userMapper: Mapper[User] = AppConfiguration.userMapper
  })

  def printRequestMethod(req: HttpRequest): Unit = logger.info(s"${req.method.name()}: ${req.entity}")
  val logRequestPrintln = DebuggingDirectives.logRequest(LoggingMagnet(_ => printRequestMethod))

  val binding = Http().bindAndHandle(richRouter.route, "localhost", 8080)
  binding.failed.foreach { ex =>
    logger.error(ex.getMessage)
  }
}
