package kamon.opentracing

import io.opentracing.ActiveSpan.{Continuation => OpenContinuation}
import io.opentracing.{ActiveSpan => OpenActiveSpan}
import kamon.trace.{Continuation => KamonContinuation}

object Continuation {
  def wrap(kamonContinuation: KamonContinuation): OpenContinuation = new Continuation(kamonContinuation)
}

class Continuation private (protected val wrapped: KamonContinuation) extends OpenContinuation {
  def unwrap: KamonContinuation = wrapped
  def activate(): OpenActiveSpan = ActiveSpan.wrap(wrapped.activate)
}
