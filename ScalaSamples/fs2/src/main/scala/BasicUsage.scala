import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.regex.Pattern

import cats.effect._
import fs2._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.io.Source
import scala.util.{Success, Try}

object FetchUrl extends App {
  lazy val source = Source.fromURL("https://stackoverflow.com/")
  val pattern = Pattern.compile("<(a).*?>.*?<[\\/](\\1)>")
  val link = Pattern.compile(".*hyperlink.*").asPredicate()

  val result = Stream
    .bracket(IO(source.getLines().mkString))(_ => IO(source.close()))
    .map(pattern.matcher(_))
    .flatMap(m => {
      val groups = scala.collection.mutable.ArrayBuffer[String]()
      while (m.find()) {
        groups += m.group()
      }

      Stream(groups: _*)
    })
    .map(s => Try(xml.XML.loadString(s)))
    .collect {
      case Success(x) => x
    }
    .filter(e => link.test((e \ "@class").text))
    .compile
    .toList
    .unsafeRunSync()

  println(result)
}

object Pipe extends App {
  def pipe[F[_], R](f: R => R): Pipe[F, R, R] = {
    def go(s: Stream[F, R]): Pull[F, R, Unit] = {
      s.pull.uncons.flatMap {
        case Some((chunk, tail)) => Pull.output(chunk.map(f.apply)) >> go(tail)
        case None => Pull.done
      }
    }

    in => go(in).stream
  }

  println(Stream(1, 2, 3, 4, 5).through(pipe[fs2.Pure, Int](i => i * 2)).toList)

  def takeWhile[F[_], R](f: R => Boolean): Pipe[F, R, R] = {
    def go(s: Stream[F, R]): Pull[F, R, Unit] = {
      s.pull.uncons.flatMap {
        case Some((chunk, tail)) =>
          chunk.indexWhere(!f(_)) match {
            case None => Pull.output(chunk) >> go(tail)
            case Some(idx) => Pull.output(chunk.take(idx))
          }
        case None => Pull.pure(None)
      }
    }

    in => go(in).stream
  }

  println(Stream(1, 2, 3, 4, 5).through(takeWhile[fs2.Pure, Int](i => i % 2 != 0)).toList)
}

object ErrorHandling extends App {
  val r = Stream(1, 2, 3, 4, 5)
    .handleErrorWith(err => {
      println(err.getLocalizedMessage)
      Stream.emit(-1).covary[IO]
    })
    .covary[IO]
    .map(_ * 2)
    .compile
    .toList
    .unsafeRunSync()

  println(r)
}

object Concurrency extends App {
  import cats.effect.ContextShift

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  val s1 = Stream(1, 2, 3)
  val s2 = Stream(4, 5, 6)

  println(s1.merge(s2.covary[IO]).compile.toList.unsafeRunSync())
}

object FileSource extends IOApp {
  import cats.implicits._

  override def run(args: List[String]): IO[ExitCode] = {
    val resource: Resource[IO, ExecutionContextExecutorService] = Resource.make(IO(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))))(ec => IO(ec.shutdown()))
    Stream.resource(resource).flatMap(ec => {
      io
        .file
        .readAll[IO](Paths.get("input.txt"), Blocker.liftExecutionContext(ec), 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .filter(_.length < 10)
        .through(text.utf8Encode)
        .through(io.file.writeAll[IO](Paths.get("output.txt"), Blocker.liftExecutionContext(ec)))
    })
      .compile.drain.as(ExitCode.Success)
  }
}