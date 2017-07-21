package kamon

import java.lang.{Iterable => JIterable}
import java.util.{AbstractMap => JAbstractMap, Map => JMap}

import io.opentracing.propagation.{TextMap => OpenTextMap}
import kamon.trace.{TextMap => KamonTextMap}

import scala.collection.JavaConverters._

package object opentracing {
  implicit class RichJavaMap(base: JMap[String, _]) {
    def asScalaStr: Map[String, String] = base.asScala.toMap.mapValues(_.toString)
  }

  implicit class RichTextMap(base: OpenTextMap) {
    def asKamon: KamonTextMap = {
      val kamonTextMap = KamonTextMap.Default()
      base.iterator.asScala.foreach(e => kamonTextMap.put(e.getKey, e.getValue))
      kamonTextMap
    }
  }

  def mapToJavaIterator(map: Map[String, String]): JIterable[JMap.Entry[String, String]] = {
    def toEntry(e: (String, String)): JMap.Entry[String, String] = new JAbstractMap.SimpleEntry(e._1, e._2)
    map.map(toEntry).asJava
  }
}
