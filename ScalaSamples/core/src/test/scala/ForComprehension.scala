import org.scalatest.{FunSuite, Matchers}

import scala.collection.immutable

class ForComprehension extends FunSuite with Matchers {
  test("basic") {
    val a = 1
    val b = 2
    val c = 3
    val d = 4

    val r: Option[Int] = for {
      a: Int <- Option(a)
      b: Int <- Option(b)
      c: Int <- Option(c)
      d: Int <- Option(d)
    } yield a + b + c + d

    assert(r.contains(10))
  }

  test("filter") {
    val a = List(Option(1), Option(2))
    val b = Option(2)
    val c = Option(3)

    val r: immutable.Seq[Int] = for {
      i: Option[Int] <- a
      z: Int <- i
      if z % 2 == 0
      j: Int <- b
      k: Int <- c
    } yield z + j + k

    assert(r.contains(7))
  }

  test("either") {
    val a: Either[Nothing, Int] = Right(1)
    val b: Either[Nothing, Int] = Right(2)
    val c: Either[String, Int] = Left("Nope")

    val r: Either[String, Int] = for {
      i <- a
      j <- b
      k <- c
    } yield i + j + k

    assert(r.isLeft)
  }

  test("sum") {
    val a = Option(1)
    val b = Option(2)
    val c = Option(3)

    val r: Option[Int] = for {
      i <- a
      j <- b
      k <- c
      ii = (1 to i).sum
      jj = (1 to j).sum
      kk = (1 to k).sum
    } yield ii + jj + kk

    assert(r.contains(10))
  }
}
