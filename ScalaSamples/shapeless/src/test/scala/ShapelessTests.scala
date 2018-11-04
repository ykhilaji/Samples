import org.scalatest.{FunSuite, Matchers}
import shapeless._
import shapeless.ops.hlist._

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

  test("zip with index") {
    val l = "a" :: "b" :: "c" :: HNil
    implicit val z = new Zip[String :: String :: String :: HNil] {
      override type Out = (String, Int) :: (String, Int) :: (String, Int) :: HNil

      override def apply(t: String :: String :: String :: HNil): ::[(String, Int), (String, Int) :: (String, Int) :: HNil] = (t.head, 1) :: (t.tail.head, 2) :: (t.tail.tail.head, 3) :: HNil

    }
    val zipped = l.zip
    assert(("a",1) :: ("b",2) :: ("c",3) :: HNil == zipped)
  }

  test("HList to List using function") {
    trait A
    case class B(a: Int) extends A
    case class C(a: Int, b: String, c: Double) extends A
    def foo[H <: HList, T <: A](a: T)(implicit gen: Generic.Aux[T, H], toTraversable: ToTraversable.Aux[H, List, Any]): List[Any] = gen.to(a).toList
    val b = B(1)
    val c = C(1, "2", 3.0)
    assert(List(1) == foo(b))
    assert(List(1, "2", 3.0) == foo(c))
  }

  test("HList to mapped List") {
    case class B(a: Int)
    case class C(a: Int, b: String, c: Double, d: Float)
    trait lowPriorityToStr extends Poly1 {
      implicit def default[A] = at[A](_ => "some LP string")
    }

    object toStr extends lowPriorityToStr {
      implicit val atInt = at[Int](_ => "some string from int")
      implicit val atString = at[String](_ => "some string from string")
      implicit val atDouble = at[Double](_ => "some string from double")
    }

    val genB = Generic[B]
    val genC = Generic[C]
    val b = B(1)
    val c = C(1, "2", 3.0, 4)

    assert(List("some string from int") == genB.to(b).map(toStr).toList)
    assert(List("some string from int", "some string from string", "some string from double", "some LP string") == genC.to(c).map(toStr).toList)
  }

  test("map hlist using function") {
    case class A(a: Int, b: Int, c: Int)
    trait lowPriorityMap extends Poly1 {
      implicit def default[A] = at[A](identity)
    }

    object map extends lowPriorityMap {
      implicit def caseInt = at[Int](i => i * 2)
    }

    def foo[H <: HList, L <: HList](a: A)(implicit gen: Generic.Aux[A, H], mapper: Mapper.Aux[map.type, H, L]) = gen.to(a).map(map)

    val a = A(1, 2, 3)
    assert(2 :: 4 :: 6 :: HNil == foo(a))
  }
}
