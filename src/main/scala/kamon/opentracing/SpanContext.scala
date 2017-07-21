package kamon.opentracing

import java.lang.{Iterable => JavaIterable}
import java.util.{Map => JavaMap}

import io.opentracing.{SpanContext => OpenSpanContext}
import kamon.trace.{SpanContext => KamonSpanContext}

object SpanContext {
  def wrap(kamonSpanContext: KamonSpanContext): OpenSpanContext = new SpanContext(kamonSpanContext)
}

class SpanContext private (protected val wrapped: KamonSpanContext) extends OpenSpanContext {
  def unwrap: KamonSpanContext = wrapped
  override def baggageItems: JavaIterable[JavaMap.Entry[String, String]] = mapToJavaIterator(wrapped.baggage.getAll)
}
