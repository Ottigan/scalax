package com.github.t3hnar.scalax.util

import org.specs2.mutable.Specification
import java.util.concurrent.TimeUnit
import org.specs2.specification.Scope
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

/**
 * @author Yaroslav Klymko
 */
class ExpiringCacheSpec extends Specification {
  "ExpiringCache" should {
    "clean expired values if get enough queries" in new ExpiringCacheScope {
      cache.map must haveSize(0)
      cache.queryCount mustEqual 0

      cache.put(0, "0")
      cache.get(0) must beSome("0")
      cache.map must haveSize(1)
      cache.queryCount mustEqual 1

      current = cache.unit.toMillis(cache.duration)

      cache.put(1, "1")
      cache.get(1) must beSome("1")
      cache.queryCount mustEqual 2

      (0 to cache.queryOverflow).foreach(_ => cache.get(3))

      cache.map.size must eventually(beEqualTo(1))
      cache.get(1) must beSome("1")
    }
    "not return expired values which are not cleaned" in new ExpiringCacheScope {
      cache.map must haveSize(0)
      cache.queryCount mustEqual 0

      cache.put(0, "0")
      cache.get(0) must beSome("0")
      cache.map.size must eventually(beEqualTo(1))

      current = cache.unit.toMillis(cache.duration)

      cache.get(0) must beNone
      cache.map.size must eventually(beEqualTo(1))
    }
    "function correctly when remove() is invoked" in new ExpiringCacheScope {
      cache.put(0, "0")
      cache.remove(0) must beSome("0")
      cache.remove(0) must beNone
    }
    "return previous value if duplicate key added" in new ExpiringCacheScope {
      cache.put(0, "0") must beNone
      cache.put(0, "5") must beSome("0")
    }
    "work correctly when contructed through auxiliary" in new ExpiringCacheScope {
      val auxiliaryConstructor = new ExpiringCache[Int, String](FiniteDuration.apply(1, TimeUnit.MILLISECONDS), 5)
      auxiliaryConstructor.put(0, "0")

      cache.unit mustEqual auxiliaryConstructor.unit
      cache.duration mustEqual auxiliaryConstructor.duration
      cache.queryOverflow mustEqual auxiliaryConstructor.queryOverflow
      cache.ec mustEqual auxiliaryConstructor.ec
    }
  }

  class ExpiringCacheScope extends Scope {
    var current = 0L
    val cache = new ExpiringCache[Int, String](1, TimeUnit.MILLISECONDS, 5) {
      override def currentMillis = current
    }
  }
}

