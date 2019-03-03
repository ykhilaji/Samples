package sample

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.logging.log4j.LogManager

import scala.concurrent.duration.FiniteDuration
import scala.io.StdIn

object AkkaWebSocketSample extends App {
  val logger = LogManager.getLogger("akka")
  implicit val system = ActorSystem("websocketSample")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  sealed trait WSMessage

  case object Tick extends WSMessage

  class Context(@volatile var firstMessage: String = "", @volatile var init: Boolean = false)

  object Context {
    def unapply(arg: Context): Option[(String, Boolean)] = Some((arg.firstMessage, arg.init))
  }

  val broadcast = Source.tick[WSMessage](FiniteDuration(1, TimeUnit.SECONDS), FiniteDuration(5, TimeUnit.SECONDS), Tick)

  val handler: Flow[Message, Message, Any] = Flow[Message]
    .mapConcat {
      case m: TextMessage => TextMessage(Source.single("echo: ") ++ m.textStream) :: Nil
      case b: BinaryMessage =>
        b.dataStream.runWith(Sink.ignore)
        Nil
    }
    .mapAsync(2)(msg => msg.textStream.runFold("")(_ + _))
    .statefulMapConcat(() => {
      val ctx = new Context()
      m => (ctx, m) :: Nil
    })
    .mapConcat {
      case (ctx: Context, msg: String) if !ctx.init =>
        logger.info(s"Set context. First message: $msg")
        ctx.firstMessage = msg
        ctx.init = true
        Nil
      case msg => msg :: Nil
    }
    .merge(broadcast)
    .collect {
      case (Context(msg, _), nextMsg) => TextMessage(s"First message: $msg. Current message: $nextMsg")
      case Tick => TextMessage(LocalDateTime.now().toString)
    }

  val route = path("ws") {
    pathEndOrSingleSlash {
      handleWebSocketMessages(handler)
    }
  }

  logger.info("Starting app")
  val binding = Http().bindAndHandle(route, "localhost", 8080)
  logger.info("App started")

  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  logger.info("App stopped")
}
