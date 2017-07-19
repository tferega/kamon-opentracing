package kamon.opentracing

import io.opentracing.Tracer.{SpanBuilder => OpenSpanBuilder}
import io.opentracing.{References, ActiveSpan => OpenActiveSpan, BaseSpan => OpenBaseSpan, Span => OpenSpan, SpanContext => OpenSpanContext}
import kamon.trace.Tracer.{SpanBuilder => KamonSpanBuilder}
import org.slf4j.LoggerFactory

object SpanBuilder {
  def wrap(kamonSpanBuilder: KamonSpanBuilder): OpenSpanBuilder = new SpanBuilder(kamonSpanBuilder)
}

class SpanBuilder private(protected val wrapped: KamonSpanBuilder) extends OpenSpanBuilder {
  private val logger = LoggerFactory.getLogger(getClass)

  def unwrap: KamonSpanBuilder = wrapped

  override def start(): OpenSpan = Span.wrap(wrapped.start)

  override def asChildOf(parent: OpenSpanContext): OpenSpanBuilder = {
    parent match {
      case null                 =>
      case context: SpanContext => wrapped.asChildOf(context.unwrap)
      case _                    => logger.error("Can't extract the parent ID from a non-Kamon SpanContext")
    }
    this
  }

  override def asChildOf(parent: OpenBaseSpan[_]): OpenSpanBuilder = asChildOf(parent.context)

  override def addReference(referenceType: String, referencedContext: OpenSpanContext): OpenSpanBuilder = {
    referenceType match {
      case null =>
      case References.CHILD_OF | References.FOLLOWS_FROM => asChildOf(referencedContext)
      case _ => logger.error(s"Unsupported reference $referenceType")
    }
    this
  }

  override def withTag(key: String, value: String): OpenSpanBuilder = {
    wrapped.withSpanTag(key, value)
    this
  }

  override def withTag(key: String, value: Boolean): OpenSpanBuilder = {
    wrapped.withSpanTag(key, value.toString)
    this
  }

  override def withTag(key: String, value: Number): OpenSpanBuilder = {
    wrapped.withSpanTag(key, value.toString)
    this
  }

  override def startManual(): OpenSpan = Span.wrap(wrapped.start)

  override def withStartTimestamp(microseconds: Long): OpenSpanBuilder = SpanBuilder.wrap(wrapped.withStartTimestamp(microseconds))

  override def startActive(): OpenActiveSpan = ActiveSpan.wrap(wrapped.startActive)

  override def ignoreActiveSpan(): OpenSpanBuilder = SpanBuilder.wrap(wrapped.ignoreActiveSpan)
}
