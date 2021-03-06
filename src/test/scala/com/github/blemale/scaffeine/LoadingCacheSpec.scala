package com.github.blemale.scaffeine

import org.scalatest._

class LoadingCacheSpec
    extends WordSpec
    with ShouldMatchers
    with OptionValues {

  "LoadingCache" should {
    "be a cache" in {
      val cache = Scaffeine().build[String, String]((key: String) => "computed")

      cache shouldBe a[Cache[_, _]]
    }

    "get or load value" in {
      val cache = Scaffeine().build[String, String]((key: String) => "computed")
      cache.put("foo", "present")

      val fooValue = cache.get("foo")
      val barValue = cache.get("bar")

      fooValue should be("present")
      barValue should be("computed")
    }

    "get or load all given values" in {
      val cache = Scaffeine().build[String, String]((key: String) => "computed")
      cache.put("foo", "present")

      val values = cache.getAll(List("foo", "bar"))

      values should contain only ("foo" -> "present", "bar" -> "computed")
    }

    "get or bulk load all given values" in {
      val cache =
        Scaffeine()
          .build[String, String](
            loader = (key: String) => "computed",
            allLoader = Some((keys: Iterable[String]) => keys.map(_ -> "bulked").toMap)
          )
      cache.put("foo", "present")

      val values = cache.getAll(List("foo", "bar"))

      values should contain only ("foo" -> "present", "bar" -> "bulked")
    }

    "refresh value with loader when no refresh loader provided" in {
      val cache =
        Scaffeine()
          .executor(DirectExecutor)
          .build[String, String]((key: String) => "computed")

      cache.put("foo", "present")
      cache.refresh("foo")
      val fooValue = cache.get("foo")

      fooValue should be("computed")
    }

    "refresh value wih refresh loader when provided" in {
      val cache =
        Scaffeine()
          .executor(DirectExecutor)
          .build[String, String](
            loader = (key: String) => "computed",
            reloadLoader = Some((key: String, old: String) => "reload")
          )

      cache.put("foo", "present")
      cache.refresh("foo")
      val fooValue = cache.get("foo")

      fooValue should be("reload")
    }
  }

}
