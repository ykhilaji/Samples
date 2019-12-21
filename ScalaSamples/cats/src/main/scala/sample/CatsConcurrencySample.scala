package sample

import java.util.concurrent.{Executors, TimeUnit}

import cats.effect.{Concurrent, ContextShift, ExitCode, Fiber, IO, IOApp, Sync, Timer}
import cats.implicits._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object BlockingOperation extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor()))
    val blockingEC = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    for {
      _ <- printCurrentThread() // main thread
      _ <- contextShift.evalOn(blockingEC)(blockingOperation()) // blocking pool
      _ <- printCurrentThread() // context shift pool
    } yield ExitCode.Success
  }

  def blockingOperation(): IO[Unit] = for {
    _ <- IO(println("Start blocking operation"))
    _ <- IO(println(s"Inside blocking operation: ${Thread.currentThread().getName}"))
    _ <- IO(Thread.sleep(1000))
    _ <- IO(println("End blocking operation"))
  } yield ()

  def printCurrentThread(): IO[Unit] = IO {
    println(Thread.currentThread().getName)
  }
}

object ShiftSample extends App {
  override def main(args: Array[String]): Unit = {
    // if thread count in pool is less than tasks count then it is needed to use IO.shift to
    // give time for other tasks
    val ecOne = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    val ecTwo = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    /*
    output without shifting:
    [pool-1-thread-1]: 1
    [pool-2-thread-1]: 2
    [pool-1-thread-1]: 1
    [pool-2-thread-1]: 2
    [pool-1-thread-1]: 1

    output with shifting:
    [pool-1-thread-1]: 1
    [pool-2-thread-1]: 2
    [pool-1-thread-1]: 11
    [pool-2-thread-1]: 22
    [pool-1-thread-1]: 1
    [pool-2-thread-1]: 2
    [pool-1-thread-1]: 11
    [pool-2-thread-1]: 22
     */


    // if thread count in pool is more or equal than tasks count then it is NOT needed to use IO.shift
//    val ecOne = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
//    val ecTwo = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    /*
    output:
    [pool-1-thread-1]: 1
    [pool-2-thread-1]: 2
    [pool-1-thread-2]: 11
    [pool-2-thread-2]: 22
     */


    val csOne: ContextShift[IO] = IO.contextShift(ecOne)
    val csTwo: ContextShift[IO] = IO.contextShift(ecTwo)

    val run = for {
      _ <- printCurrentThread(1)(csOne)
      _ <- printCurrentThread(2)(csTwo)
      _ <- printCurrentThread(11)(csOne)
      _ <- printCurrentThread(22)(csTwo)
    } yield ()

    run.unsafeRunSync()
  }

  def printCurrentThread(i: Int)(implicit cs: ContextShift[IO]): IO[Fiber[IO, Unit]] = {
    def inner(): IO[Unit] = {
      val computation = for {
        _ <- IO(println(s"[${Thread.currentThread().getName}]: $i"))
        _ <- IO(Thread.sleep(1000))
      } yield ()

//      computation.flatMap(_ => inner())
      computation.flatMap(_ => IO.shift *> inner())
    }

    inner().start
  }
}

object Concurrently extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2)))

    for {
      _ <- printCurrentThread()
      fiber1 <- blockingOperation(1).start(contextShift)
      fiber2 <- blockingOperation(2).start(contextShift)
      _ <- printCurrentThread()
      _ <- fiber1.join
      _ <- fiber2.join
      _ <- printCurrentThread()
    } yield ExitCode.Success
  }

  def blockingOperation(i: Int): IO[Unit] = for {
    _ <- IO(println(s"Start blocking operation[$i]"))
    _ <- IO(println(s"Inside blocking operation[$i]:  ${Thread.currentThread().getName}"))
    _ <- IO(Thread.sleep(1000))
    _ <- IO(println(s"End blocking operation[$i]"))
  } yield ()

  def printCurrentThread(): IO[Unit] = IO {
    println(Thread.currentThread().getName)
  }
}

object TimeoutForBlockingOperation extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- printCurrentThread[IO]("main")
    _ <- blockingOperation[IO]
    _ <- printCurrentThread[IO]("main")
  } yield ExitCode.Success

  def blockingOperation[F[_] : Timer](implicit F: Concurrent[F]): F[Unit] = {
    def go(): F[Unit] = for {
      _ <- F.delay(println("Start"))
      _ <- printCurrentThread("blockingOperation")
      _ <- F.delay(Thread.sleep(1000))
      _ <- F.delay(println("End"))
      _ <- printCurrentThread("blockingOperation")
    } yield ()

    // will switch on the another thread.
    // Under the hood there is F.race(go(), Timeout(duration)) -> 2 tasks run in parallel
    Concurrent.timeout(go(), FiniteDuration(2000, TimeUnit.MILLISECONDS))
  }

  def printCurrentThread[F[_]](task: String)(implicit F: Sync[F]): F[Unit] =
    F.delay(println(s"[$task] ${Thread.currentThread().getName}"))
}