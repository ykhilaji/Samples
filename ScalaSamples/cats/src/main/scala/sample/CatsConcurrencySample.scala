package sample

import java.util.concurrent.Executors

import cats.effect.{ContextShift, ExitCode, Fiber, IO, IOApp}
import cats.implicits._

import scala.concurrent.ExecutionContext

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