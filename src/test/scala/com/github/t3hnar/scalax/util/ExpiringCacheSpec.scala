package com.github.t3hnar.scalax.util

import org.specs2.mutable.Specification
import java.util.concurrent.TimeUnit

import org.specs2.specification.Scope

import scala.concurrent.ExecutionContext

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
    "remove values successfully" in new ExpiringCacheScope {
      cache.put(0, "0")
      cache.remove(0) must beSome("0")
    }
    "return previous value if duplicate key added" in new ExpiringCacheScope {
      cache.put(0, "0")
      cache.put(0, "5") must beSome("0")
    }
  }

  class ExpiringCacheScope extends Scope {
    var current = 0L
    val cache = new ExpiringCache[Int, String](1, TimeUnit.MILLISECONDS, 5) {
      override def currentMillis = current
    }
  }
}

