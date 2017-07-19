package kamon.opentracing

import java.util.{Map => JavaMap}

import io.opentracing.{BaseSpan => OpenBaseSpan, SpanContext => OpenSpanContext}
import kamon.trace.{Span => KamonSpan}

object BaseSpan {
  private val AnnotationName = "log"
  private val EventName = "event"
}

abstract class BaseSpan[T <: OpenBaseSpan[T]] extends OpenBaseSpan[T] { this: T =>
  import BaseSpan._

  protected val wrapped: KamonSpan
  protected val openContext: OpenSpanContext

  override def log(fields: JavaMap[String, _]): T = {
    wrapped.annotate(AnnotationName, fields.asScalaStr)
    this
  }

  override def log(timestampMicroseconds: Long, fields: JavaMap[String, _]): T = {
    wrapped.annotate(timestampMicroseconds, AnnotationName, fields.asScalaStr)
    this
  }

  override def log(event: String): T = {
    val fields = Map(EventName -> event)
    wrapped.annotate(AnnotationName, fields)
    this
  }

  override def log(timestampMicroseconds: Long, event: String): T = {
    val fields = Map(EventName -> event)
    wrapped.annotate(timestampMicroseconds, AnnotationName, fields)
    this
  }

  override def log(eventName: String, payload: scala.Any): T = {
    val fields = Map(eventName -> payload.toString)
    wrapped.annotate(AnnotationName, fields)
    this
  }

  override def log(timestampMicroseconds: Long, eventName: String, payload: scala.Any): T = {
    val fields = Map(eventName -> payload.toString)
    wrapped.annotate(timestampMicroseconds, AnnotationName, fields)
    this
  }

  override def getBaggageItem(key: String): String = wrapped.getBaggage(key).orNull

  override def context: OpenSpanContext = openContext

  override def setTag(key: String, value: String): T = {
    wrapped.addSpanTag(key, value)
    this
  }

  override def setTag(key: String, value: Boolean): T = {
    wrapped.addSpanTag(key, value.toString)
    this
  }

  override def setTag(key: String, value: Number): T = {
    wrapped.addSpanTag(key, value.toString)
    this
  }

  override def setBaggageItem(key: String, value: String): T = {
    wrapped.addBaggage(key, value)
    this
  }

  override def setOperationName(operationName: String): T = {
    wrapped.setOperationName(operationName)
    this
  }
}
