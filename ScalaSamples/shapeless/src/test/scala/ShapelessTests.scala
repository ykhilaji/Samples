import org.scalatest.{FunSuite, Matchers}
import shapeless._

class ShapelessTests extends FunSuite with Matchers {
  test("shapeless list") {
    val x = 1 :: "string" :: 1.5 :: HNil

    assert(1 == x.select[Int])
    assert("string" == x.select[String])
    assert(1.5 == x.select[Double])
    assert(1 == x.head)
    assert("string" :: 1.5 :: HNil == x.tail)
  }

  test("shapeless and polymorphic functions") {
    object PolyFunc extends Poly1 {
      implicit def caseInt = at[Int] {_ * 2}
      implicit def caseString = at[String] {_.length}
      implicit def caseDouble = at[Double] {_.floor}
    }

    val x = 1 :: "string" :: 1.5 :: HNil
    val y = x.map(PolyFunc)
    assert(2 :: 6 :: 1 :: HNil == y)
  }

  test("shapeless and case class") {
    case class A(a: Int, b: String, c: Double)
    val x = Generic[A]
    val a = A(1, "2", 3.0)
    assert(x.to(a) == 1 :: "2" :: 3.0 :: HNil)
    assert(a == x.from(1 :: "2" :: 3.0 :: HNil))
  }

  test("lenses") {
    case class A(a: Int, b: String, c: Double)
    val aLens = lens[A].a
    val a = A(1, "2", 3.0)
    assert(A(2, "2", 3.0) == aLens.set(a)(2))
  }
}
