package com.github.blemale.scaffeine

import java.util.concurrent.Executor

import com.github.benmanes.caffeine.cache.AsyncLoadingCache

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.FunctionConverters._

import scala.concurrent.{ExecutionContext, Future}

object SAsyncLoadingCache {
  def apply[K, V](asyncLoadingCache: AsyncLoadingCache[K, V]): SAsyncLoadingCache[K, V] =
    new SAsyncLoadingCache(asyncLoadingCache)
}

class SAsyncLoadingCache[K, V](val underlying: AsyncLoadingCache[K, V]) {

  def getIfPresent(key: K)(implicit ec: ExecutionContext): Future[V] =
    underlying.getIfPresent(key).toScala

  def get(key: K, mappingFunction: K => V)(implicit ec: ExecutionContext): Future[V] =
    underlying.get(key, asJavaFunction(mappingFunction)).toScala

  def getFuture(key: K, mappingFunction: K => Future[V])(implicit ec: ExecutionContext): Future[V] =
    underlying.get(
      key,
      asJavaBiFunction((k: K, _: Executor) => mappingFunction(k).toJava.toCompletableFuture)
    ).toScala

  def get(key: K)(implicit ec: ExecutionContext): Future[V] =
    underlying.get(key).toScala

  def getAll(keys: Iterable[K])(implicit ec: ExecutionContext): Future[Map[K, V]] =
    underlying.getAll(keys.asJava).toScala.map(_.asScala.toMap)

  def put(key: K, valueFuture: Future[V])(implicit ec: ExecutionContext): Unit =
    underlying.put(key, valueFuture.toJava.toCompletableFuture)

  def synchronous(): SLoadingCache[K, V] =
    SLoadingCache(underlying.synchronous())

  override def toString = s"SAsyncLoadingCache($underlying)"
}