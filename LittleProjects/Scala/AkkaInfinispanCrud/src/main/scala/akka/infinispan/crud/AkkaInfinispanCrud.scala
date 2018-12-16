package akka.infinispan.crud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.infinispan.crud.common.DataSource
import akka.infinispan.crud.web.HttpListener
import akka.stream.ActorMaterializer
import org.apache.logging.log4j.LogManager

object AkkaInfinispanCrud extends App {
  val logger = LogManager.getLogger("akka-infinispan-crud")
  logger.info("Star AkkaInfinispanCrud app")

  implicit val system = ActorSystem("akkaInfinispanCrud")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  Http().bindAndHandle(HttpListener().api, "localhost", 8080)

  sys.addShutdownHook(() => {
    logger.info("Trying to stop cache manager")
    DataSource.cacheManager.stop()
    logger.info("Successfully stopped cache manager")
    logger.info("Trying to stop actor system")
    system.terminate().onComplete({
      case scala.util.Success(_) => logger.info("Successfully stopped actor system")
      case scala.util.Failure(exception) => logger.error(exception.getLocalizedMessage)
    })
  })
}
