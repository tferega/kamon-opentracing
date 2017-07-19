package kamon.opentracing

import java.nio.ByteBuffer

import io.opentracing.propagation.{Format => OpenFormat, TextMap => OpenTextMap}
import io.opentracing.{ActiveSpan => OpenActiveSpan, Span => OpenSpan, SpanContext => OpenSpanContext, Tracer => OpenTracer}
import kamon.trace.SpanContextCodec.{Format => KamonFormat}
import kamon.trace.{Tracer => KamonTracer}
import org.slf4j.LoggerFactory

object Tracer {
  def wrap(kamonTracer: KamonTracer): OpenTracer = new Tracer(kamonTracer)
}

class Tracer private (protected val wrapped: KamonTracer) extends OpenTracer {
  private val logger = LoggerFactory.getLogger(getClass)

  def unwrap: KamonTracer = wrapped

  def buildSpan(operationName: String): OpenTracer.SpanBuilder = SpanBuilder.wrap(wrapped.buildSpan(operationName))

  def extract[C](format: OpenFormat[C], carrier: C): OpenSpanContext = {
    val context = format match {
      case OpenFormat.Builtin.BINARY =>
        val kamonCarrier = carrier.asInstanceOf[ByteBuffer]
        wrapped.extract(KamonFormat.Binary, kamonCarrier)
      case OpenFormat.Builtin.HTTP_HEADERS =>
        val kamonCarrier = carrier.asInstanceOf[OpenTextMap].asKamon
        wrapped.extract(KamonFormat.HttpHeaders, kamonCarrier)
      case OpenFormat.Builtin.TEXT_MAP =>
        val kamonCarrier = carrier.asInstanceOf[OpenTextMap].asKamon
        wrapped.extract(KamonFormat.TextMap, kamonCarrier)
      case _ =>
        logger.error(s"Format $format not supported!")
        None
    }
    SpanContext.wrap(context.orNull)
  }

  def inject[C](spanContext: OpenSpanContext, format: OpenFormat[C], carrier: C): Unit = spanContext match {
    case context: SpanContext =>
      format match {
        case OpenFormat.Builtin.BINARY =>
          val kamonCarrier = carrier.asInstanceOf[ByteBuffer]
          wrapped.inject(context.unwrap, KamonFormat.Binary, kamonCarrier)
        case OpenFormat.Builtin.HTTP_HEADERS =>
          val kamonCarrier = carrier.asInstanceOf[OpenTextMap].asKamon
          wrapped.inject(context.unwrap, KamonFormat.HttpHeaders, kamonCarrier)
        case OpenFormat.Builtin.TEXT_MAP =>
          val kamonCarrier = carrier.asInstanceOf[OpenTextMap].asKamon
          wrapped.inject(context.unwrap, KamonFormat.TextMap, kamonCarrier)
        case _ =>
          logger.error(s"Format $format not supported!")
          None
      }
    case _ => logger.error("Can't extract the parent ID from a non-Kamon SpanContext")
  }


  def activeSpan(): OpenActiveSpan = ActiveSpan.wrap(wrapped.activeSpan)

  def makeActive(span: OpenSpan): OpenActiveSpan = span match {
    case span: Span => ActiveSpan.wrap(wrapped.makeActive(span.unwrap))
    case _ =>
      logger.error("Can't extract the parent ID from a non-Kamon SpanContext")
      null
  }
}
