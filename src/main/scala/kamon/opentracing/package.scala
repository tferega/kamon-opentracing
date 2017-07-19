package kamon

import java.util.{Map => JavaMap}

import io.opentracing.propagation.{TextMap => OpenTextMap}
import kamon.trace.{TextMap => KamonTextMap}

import scala.collection.JavaConverters._

package object opentracing {
  implicit class RichJavaMap(base: JavaMap[String, _]) {
    def asScalaStr: Map[String, String] = base.asScala.toMap.mapValues(_.toString)
  }

  implicit class RichTextMap(base: OpenTextMap) {
    def asKamon: KamonTextMap = {
      val kamonTextMap = KamonTextMap.Default()
      base.iterator.asScala.foreach(e => kamonTextMap.put(e.getKey, e.getValue))
      kamonTextMap
    }
  }
}
