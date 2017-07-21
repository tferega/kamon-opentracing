package kamon.opentracing
package test

import java.util.{Iterator => JIterator, Map => JMap}

import io.opentracing.propagation.TextMap

import scala.collection.mutable.{HashMap => MHashMap}

class TestTextMap(values: (String, String)*) extends TextMap {
  private object Lock
  private val map: MHashMap[String, String] = MHashMap(values: _*)
  override def put(key: String, value: String): Unit = Lock.synchronized(map.put(key, value))
  override def iterator(): JIterator[JMap.Entry[String, String]] = Lock.synchronized(mapToJavaIterator(map.toMap).iterator)
  def get(key: String): Option[String] = map.get(key)
}
