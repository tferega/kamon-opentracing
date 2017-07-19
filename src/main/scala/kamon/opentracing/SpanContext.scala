package kamon.opentracing

import java.lang.{Iterable => JavaIterable}
import java.util.{AbstractMap => JavaAbstractMap, Map => JavaMap}

import io.opentracing.{SpanContext => OpenSpanContext}
import kamon.trace.{SpanContext => KamonSpanContext}

import scala.collection.JavaConverters._

object SpanContext {
  def wrap(kamonSpanContext: KamonSpanContext): OpenSpanContext = new SpanContext(kamonSpanContext)
}

class SpanContext private (protected val wrapped: KamonSpanContext) extends OpenSpanContext {
  def unwrap: KamonSpanContext = wrapped

  override def baggageItems: JavaIterable[JavaMap.Entry[String, String]] = {
    def toEntry(e: (String, String)): JavaMap.Entry[String, String] = new JavaAbstractMap.SimpleEntry(e._1, e._2)
    wrapped.baggage
      .getAll
      .map(toEntry)
      .asJava
  }
}
