import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.util.Try

object FutureFetchUrl extends App {
  def get(url: String)(implicit ex: ExecutionContext): Future[String] = Future {
    val source = Source.fromURL(url)
    try source.getLines().mkString finally source.close()
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  val pattern = Pattern.compile("<(a).*?>.*?<[\\/](\\1)>")
  val link = Pattern.compile(".*hyperlink.*").asPredicate()
  val body = get("https://stackoverflow.com/")
    .map(pattern.matcher(_))
    .map(m => {
      val groups = scala.collection.mutable.ArrayBuffer[String]()
      while (m.find()) {
        groups += m.group()
      }

      groups
    })
    .map(_.par.map(s => Try(xml.XML.loadString(s)).toOption).collect {
      case Some(x) => x
    })
    .map(_.filter(elem => link.test((elem \ "@class").text)))

  val result = Await.result(body, Duration(5, TimeUnit.SECONDS))
  println(result)
}
