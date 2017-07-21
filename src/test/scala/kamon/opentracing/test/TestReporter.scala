package kamon.opentracing
package test

import com.typesafe.config.Config
import kamon.trace.Span.{FinishedSpan => KamonFinishedSpan}
import kamon.{SpanReporter => KamonSpanReporter}

import scala.concurrent.duration.{Duration, DurationInt}

object TestReporter extends KamonSpanReporter {
  case class AnnotationReport(name: String, fields: Map[String, String])
  case class Report(operationName: String, traceId: String, spanId: String, parentId: String, tags: Map[String, Any], annotations: Seq[AnnotationReport])

  private object Lock
  private val DefaultDuration = 2 seconds
  private var reportStack: List[KamonFinishedSpan] = List.empty

  override def start(): Unit = ()
  override def stop(): Unit = ()
  override def reconfigure(config: Config): Unit = ()
  override def reportSpans(spans: Seq[KamonFinishedSpan]): Unit = Lock.synchronized {
    reportStack = spans ++: reportStack
  }

  def getLatestReport: Option[Report] = Lock.synchronized {
    reportStack match {
      case head :: tail =>
        reportStack = tail
        Some(fromFinishedSpan(head))
      case Nil => None
    }
  }

  def waitForReport(timeout: Duration = DefaultDuration): Option[Report] = {
    val end = System.currentTimeMillis + timeout.toMillis
    var result: Option[Report] = None
    while (System.currentTimeMillis <= end && result.isEmpty) {
      result = getLatestReport
      Thread.sleep(100)
    }
    result
  }

  private def fromFinishedSpan(finishedSpan: KamonFinishedSpan): Report = {
    Report(
      finishedSpan.operationName,
      finishedSpan.context.traceID.string,
      finishedSpan.context.spanID.string,
      finishedSpan.context.parentID.string,
      finishedSpan.tags.mapValues(_.asInstanceOf[Any]),
      finishedSpan.annotations.map(e => AnnotationReport(e.name, e.fields)))
  }
}
