package upickle
import utest._
import acyclic.file

object AutopickleCases {
  sealed trait CachedSealedTrait
  case class A(a: Int, b: String) extends CachedSealedTrait
  case object B extends CachedSealedTrait

  object CachedSealedTrait {
    val pklR: upickle.default.Reader[CachedSealedTrait] = upickle.default.macroR[CachedSealedTrait]
    implicit val pklW: upickle.default.Writer[CachedSealedTrait] = upickle.default.macroW[CachedSealedTrait]
  }

  case class CachedCaseClass(b: String, a: Double)
  object CachedCaseClass {
    implicit val pkl = upickle.default.macroRW[CachedCaseClass]
  }

  case class UncachedCaseClass(b: String, a: Double)
}

/**
  * Make sure that when you cache the implicit in the companion object, it gets
  * correctly picked up by implicit search and re-used over and over. On the
  * other hand if you use an un-cached implicit `def` you get a new instance
  * of the reader/writer each time.
  */
object AutopickleTests extends TestSuite {
  import AutopickleCases._
  val exampleString = """{"$type":"upickle.AutopickleCases.A","a":1,"b":"1"}"""
  val example: CachedSealedTrait = A(1, "1")
  val tests = TestSuite {
    "cachedWrite" - {
      val writer = implicitly[upickle.default.Writer[CachedSealedTrait]]
      assert(writer eq CachedSealedTrait.pklW)
      val written = upickle.default.write(example)(writer)
      assert(written == exampleString)
    }
    "uncachedRead" - {
      val reader = implicitly[upickle.default.Reader[CachedSealedTrait]]
      assert(!(reader eq CachedSealedTrait.pklR))
      assert(upickle.default.read(exampleString)(reader) == example)
    }
    "cachedRW" - {
      val res = upickle.default.read[CachedCaseClass](upickle.default.write(CachedCaseClass("aaa",42.0)))
      assert(res == CachedCaseClass("aaa",42.0))
      assert(implicitly[upickle.default.Reader[CachedCaseClass]] eq implicitly[upickle.default.Reader[CachedCaseClass]])
      assert(implicitly[upickle.default.Writer[CachedCaseClass]] eq implicitly[upickle.default.Writer[CachedCaseClass]])
    }
    "uncachedRW" - {
      val res = upickle.default.read[UncachedCaseClass](upickle.default.write(UncachedCaseClass("aaa",42.0)))
      assert(res == UncachedCaseClass("aaa",42.0))
      val a = implicitly[upickle.default.Reader[UncachedCaseClass]] eq implicitly[upickle.default.Reader[UncachedCaseClass]]
      val b = implicitly[upickle.default.Writer[UncachedCaseClass]] eq implicitly[upickle.default.Writer[UncachedCaseClass]]
      assert(!a)
      assert(!b)
    }
  }
}