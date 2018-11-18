import org.scalatest.{FunSuite, Matchers}

import scala.annotation.tailrec
import scala.util.Try

class FunctionalProgrammingExercises extends FunSuite with Matchers {
  test("fib test") {
    def fib(n: Int): Int = n match {
      case 0 => 1
      case 1 => 1
      case x => fib(x - 2) + fib(x - 1)
    }

    assert(1 == fib(0))
    assert(1 == fib(1))
    assert(2 == fib(2))
    assert(3 == fib(3))
    assert(5 == fib(4))
    assert(8 == fib(5))
    assert(13 == fib(6))
  }

  test("is array ordered") {
    def isOrdered[A](a: Array[A], p: (A, A) => Boolean): Boolean = {
      def helper(i: Int, l: Int): Boolean = {
        if (i > l) {
          true
        } else if (p(a(i - 1), a(i))) {
          helper(i + 1, l)
        } else {
          false
        }
      }

      if (a.isEmpty || a.length == 1) {
        true
      } else {
        helper(1, a.length - 1)
      }
    }

    assert(isOrdered[Int](Array(1, 2, 3, 4, 5), (a, b) => a < b))
    assert(!isOrdered[Int](Array(3, 2, 3, 4, 5), (a, b) => a < b))
    assert(isOrdered[Int](Array(5, 4, 3, 2, 1), (a, b) => a > b))
    assert(!isOrdered[Int](Array(5, 4, 3, 2, 3), (a, b) => a > b))
  }

  test("currying") {
    def curry[A, B, C](f: (A, B) => C): A => B => C = (a: A) => (b: B) => f(a, b)

    val firstGTsecond: (Int, Int) => Boolean = (a: Int, b: Int) => a > b
    val firstGEsecond: (Int, Int) => Boolean = (a: Int, b: Int) => a >= b
    val firstLTsecond: (Int, Int) => Boolean = (a: Int, b: Int) => a < b
    val firstLEsecond: (Int, Int) => Boolean = (a: Int, b: Int) => a <= b

    assert(curry(firstGTsecond)(2)(1))
    assert(curry(firstGEsecond)(2)(2))
    assert(curry(firstLTsecond)(5)(10))
    assert(curry(firstLEsecond)(1)(1))
  }

  test("uncurry") {
    def curry[A, B, C](f: (A, B) => C): A => B => C = (a: A) => (b: B) => f(a, b)

    def uncurry[A, B, C](f: A => B => C): (A, B) => C = (a: A, b: B) => f(a)(b)

    val curriedSum: Int => Int => Int = curry((a: Int, b: Int) => a + b)
    val uncurriedSum: (Int, Int) => Int = uncurry(curriedSum)

    assert(curriedSum(1)(2) == 3)
    assert(uncurriedSum(1, 2) == 3)
  }

  test("compose") {
    def compose[A,B,C](f: B => C, g: A => B): A => C = (a: A) => f(g(a))

    val incr = (a: Int) => a + 1
    val twice = (a: Int) => a * 2

    assert(compose(incr, twice)(1) == 3)
    assert(compose(twice, incr)(1) == 4)
    assert(compose(compose(incr, twice), compose(twice, incr))(1) == 9)
  }

  test("tail") {
    def tail(a: List[_]): List[_] = a match {
      case _ :: xs => xs
      case Nil => throw new Exception("empty list")
    }

    assert(tail(List(1, 2, 3)) == List(2, 3))
    assertThrows[Exception](tail(List()))
  }

  test("set head") {
    def setHead[A](a: List[A], head: A): List[A] = a match {
      case _ :: xs => head :: xs
      case Nil => head :: Nil
    }

    assert(List(10, 2, 3) == setHead(List(1, 2, 3), 10))
    assert(List(1) == setHead(Nil, 1))
  }

  test("drop") {
    def drop[A](l: List[A], n: Int): List[A] = l match {
      case _ :: xs if n > 0 => drop(xs, n - 1)
      case _ => l
    }

    assert(List(2, 3) == drop(List(1, 2, 3), 1))
    assert(Nil == drop(List(1), 2))
  }

  test("drop while") {
    def dropWhile[A](a: List[A], p: A => Boolean): List[A] = a match {
      case x :: xs if p(x) => dropWhile(xs, p)
      case _ => a
    }

    assert(List(1, 2) == dropWhile(List(2, 4, 6, 8, 10, 1, 2), (a: Int) => a % 2 == 0))
  }

  test("fold right") {
    def foldRight[A, B](a: List[A], b: B)(f: (A, B) => B): B = a match {
      case x :: xs => f(x, foldRight(xs, b)(f))
      case _ => b
    }

    assert(10 == foldRight(List(1, 2, 3, 4), 0)(_ + _))
    assert(24 == foldRight(List(1, 2, 3, 4), 1)(_ * _))
  }

  test("fold left") {
    @tailrec
    def foldLeft[A,B](a: List[A], b: B)(f: (B, A) => B): B = a match {
      case x :: xs => foldLeft(xs, f(b, x))(f)
      case _ => b
    }

    assert(10 == foldLeft(List(1, 2, 3, 4), 0)(_ + _))
    assert(24 == foldLeft(List(1, 2, 3, 4), 1)(_ * _))

    // reverse
    assert(List(3, 2, 1) == foldLeft(List(1, 2, 3), List[Int]())((a, b) => b :: a))
  }

  test("foldRight via foldLeft") {
    @tailrec
    def foldLeft[A,B](a: List[A], b: B)(f: (B, A) => B): B = a match {
      case x :: xs => foldLeft(xs, f(b, x))(f)
      case _ => b
    }
    // optimization
    // will not throw StackOverflow exception even for large lists
    def foldRightViaFoldLeft[A, B](a: List[A], b: B)(f: (A, B) => B): B =
      foldLeft(a.reverse, b)((a, b) => f(b, a))

    assert(10 == foldRightViaFoldLeft(List(1, 2, 3, 4), 0)(_ + _))
    assert(24 == foldRightViaFoldLeft(List(1, 2, 3, 4), 1)(_ * _))
  }

  test("increment element of list using foldRight") {
    def foldRight[A, B](a: List[A], b: B)(f: (A, B) => B): B = a match {
      case x :: xs => f(x, foldRight(xs, b)(f))
      case _ => b
    }

    assert(List(2, 3, 4) == foldRight(List(1, 2, 3), List[Int]())((a, b) => (a + 1) :: b))
  }

  test("map") {
    @tailrec
    def foldLeft[A,B](a: List[A], b: B)(f: (B, A) => B): B = a match {
      case x :: xs => foldLeft(xs, f(b, x))(f)
      case _ => b
    }
    def foldRightViaFoldLeft[A, B](a: List[A], b: B)(f: (A, B) => B): B =
      foldLeft(a.reverse, b)((a, b) => f(b, a))

    def map[A,B](a: List[A])(f: A => B): List[B] = a match {
      case _ :: _ => foldRightViaFoldLeft(a, List[B]())((b, a) => f(b) :: a)
      case Nil => Nil
    }

    assert(List(2, 4, 6) == map(List(1, 2, 3))(_ * 2))
  }

  test("flat map") {
    // not optimal implementation
    def flatMap[A,B](a: List[A])(f: A => List[B]): List[B] = a match {
      case x :: xs => f(x) ::: flatMap(xs)(f)
      case Nil => Nil
    }

    assert(List(1, 1, 2, 2, 3, 3) == flatMap(List(1, 2, 3))(a => List(a, a)))
  }

  test("filter via flatMap") {
    def flatMap[A,B](a: List[A])(f: A => List[B]): List[B] = a match {
      case x :: xs => f(x) ::: flatMap(xs)(f)
      case Nil => Nil
    }

    def filter[A](a: List[A])(f: A => Boolean): List[A] =
      flatMap(a)(a => if (f(a)) List(a) else Nil)

    assert(List(1, 3, 5) == filter(List(1, 2, 3, 4, 5))(_ % 2 == 1))
  }

  test("option") {
    def sequence[A](a: List[Option[A]]): Option[List[A]] = a match {
      case x :: xs => x.flatMap(e => sequence(xs).map(e :: _))
      case Nil => Some(Nil)
    }

    assert(sequence(List(Some(1), Some(2), Some(3))).contains(List(1, 2, 3)))
    assert(sequence(List(Some(1), None, Some(3))).isEmpty)
  }

  test("extractors") {
    object StringSeparatedByDot {
      def unapply(arg: String): Option[(String, String)] = arg.split("[.]").toList match {
        case l :: r :: Nil => Some(l, r)
        case _ => None
      }
    }

    val x: Option[(String, String)] = "abc" match {
      case StringSeparatedByDot(v) => Some(v)
      case _ => None
    }
    val y: Option[(String, String)] = "abc.def" match {
      case StringSeparatedByDot(v) => Some(v)
      case _ => None
    }

    assert(x.isEmpty)
    assert(y.contains(("abc", "def")))
  }

  test("either type") {
    def map[A, B](a: A)(f: A => B): Either[Throwable, B] = Try(Right(f(a))) match {
      case scala.util.Success(v) => v
      case scala.util.Failure(exception) => Left(exception)
    }

    assert(map(1)(_ + 1).isRight)
    assert(map(1)(_ / 0).isLeft)
  }

  test("streams") {
    def toList(s: Stream[_]): List[_] = s.toList

    var c = 0
    val x = 1 #:: {c += 1; 2} #:: {c += 1; 3} #:: {c += 1; 4} #:: {c += 1; 5} #:: Stream.empty
    assert(c == 0)
    toList(x)
    assert(c == 4)
  }

  test("stream takeN") {
    def takeN[A](s: Stream[A])(n: Int): Stream[A] = s match {
      case x #:: xs if n > 1 => x #:: takeN(xs)(n - 1)
      case x #:: _ if n == 1 => x #:: Stream.empty // important to proceed explicitly case when n == 1 to avoid evaluating of tail
      case _ => Stream.Empty
    }

    var c = 0
    val x = 1 #:: {c += 1; 2} #:: {c += 1; 3} #:: {c += 1; 4} #:: {c += 1; 5} #:: Stream.empty
    assert(c == 0)
    val l = takeN(x)(3)
    assert(l == Stream(1, 2, 3))
    assert(c == 3)
  }
}
