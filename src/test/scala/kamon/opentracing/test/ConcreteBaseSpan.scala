package kamon.opentracing
package test

import io.opentracing.{SpanContext => OpenSpanContext}
import kamon.trace.{Span => KamonSpan}

class ConcreteBaseSpan(protected val wrapped: KamonSpan)  extends BaseSpan[ConcreteBaseSpan] {
  protected override val openContext: OpenSpanContext = SpanContext.wrap(wrapped.context)
}
