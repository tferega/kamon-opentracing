package kamon.opentracing

import io.opentracing.{Span => OpenSpan, SpanContext => OpenSpanContext}
import kamon.trace.{Span => KamonSpan}

object Span {
  def wrap(kamonSpan: KamonSpan): OpenSpan = new Span(kamonSpan)
}

class Span private(protected override val wrapped: KamonSpan) extends BaseSpan[OpenSpan] with OpenSpan {
  protected override val openContext: OpenSpanContext = SpanContext.wrap(wrapped.context)

  def unwrap: KamonSpan = wrapped
  override def finish(): Unit = wrapped.finish
  override def finish(finishMicros: Long): Unit = wrapped.finish(finishMicros)
}
