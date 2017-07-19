package kamon.opentracing

import io.opentracing.ActiveSpan.{Continuation => OpenContinuation}
import io.opentracing.{ActiveSpan => OpenActiveSpan, SpanContext => OpenSpanContext}
import kamon.trace.{ActiveSpan => KamonActiveSpan}

object ActiveSpan {
  def wrap(kamonActiveSpan: KamonActiveSpan): OpenActiveSpan = new ActiveSpan(kamonActiveSpan)
}

class ActiveSpan private(protected override val wrapped: KamonActiveSpan) extends BaseSpan[OpenActiveSpan] with OpenActiveSpan {
  protected override val openContext: OpenSpanContext = SpanContext.wrap(wrapped.context)

  def unwrap: KamonActiveSpan = wrapped
  override def capture: OpenContinuation = Continuation.wrap(wrapped.capture)
  override def deactivate(): Unit = wrapped.deactivate
  override def close(): Unit = wrapped.deactivate
}
