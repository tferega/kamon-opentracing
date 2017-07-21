package kamon.opentracing

import io.opentracing.Tracer.{SpanBuilder => OpenSpanBuilder}
import io.opentracing.{References => OpenReferences, Span => OpenSpan}
import kamon.Kamon
import kamon.opentracing.test.{Harness, TestReporter}
import kamon.trace.{ActiveSpan => KamonActiveSpan}
import kamon.trace.Span.{Real => KamonRealSpan}
import org.scalactic.source.{Position => ScalaTestPosition}
import org.scalatest.DoNotDiscover
import org.scalatest.concurrent.PatienceConfiguration.Timeout

@DoNotDiscover
class SpanBuilderSpec extends Harness {
  var tracer: Tracer = new Tracer

  "SpanBuilder" should {
    "be available via the Tracer" in {
      val builder = tracer.buildSpan("")
      builder must be (a[SpanBuilder])
    }

    "start a child span from a parent span" in {
      testChildSpan(builder => parentSpan => builder.asChildOf(parentSpan))
    }

    "start a child span from parent context" in {
      testChildSpan(builder => parentSpan => builder.asChildOf(parentSpan.context))
    }

    "start a child span from CHILD_OF reference" in {
      testChildSpan(builder => parentSpan => builder.addReference(OpenReferences.CHILD_OF, parentSpan.context))
    }

    "start a child span from FOLLOWS_FROM reference" in {
      testChildSpan(builder => parentSpan => builder.addReference(OpenReferences.FOLLOWS_FROM, parentSpan.context))
    }

    "start a tagged span (string)" in {
      testTaggedSpan(_.withTag("tag", "value"), "String(value)")
    }

    "start a tagged span (long)" in {
      testTaggedSpan(_.withTag("tag", 33), "Number(33)")
    }

    "start a tagged span (boolean)" in {
      testTaggedSpan(_.withTag("tag", true), "True")
    }

    "start an inactive span" in {
      val builder = tracer.buildSpan("myspan").ignoreActiveSpan
      builder.startManual
      val inactiveSpan = tracer.activeSpan()
      inactiveSpan must be (null)
    }

    "start an active span" in {
      val builder = tracer.buildSpan("myspan").ignoreActiveSpan
      builder.startActive
      val activeSpan = tracer.activeSpan()
      activeSpan must be (an[ActiveSpan])
      val wrappedSpan = activeSpan.asInstanceOf[ActiveSpan].unwrap
      wrappedSpan must be (a[KamonActiveSpan])
      wrappedSpan.asInstanceOf[KamonActiveSpan].context.traceID.string mustNot be ("")
    }
  }

  private def testChildSpan(builderBuilder: OpenSpanBuilder => OpenSpan => OpenSpanBuilder)(implicit pos: ScalaTestPosition): Unit = {
    val name = "myspan"
    val builder = tracer.buildSpan(name)
    val kamonParentSpan = Kamon.buildSpan(name).ignoreActiveSpan.start
    val parentSpan = Span.wrap(kamonParentSpan)
    val span = builderBuilder(builder)(parentSpan).startManual()
    span must be (a[Span])
    span.finish()
    val report = waitForReport
    report.operationName must be (name)
    report.parentId must be (kamonParentSpan.context.spanID.string)
  }

  private def testTaggedSpan(tagInserter: OpenSpanBuilder => OpenSpanBuilder, tagComparator: String)(implicit pos: ScalaTestPosition): Unit = {
    val name = "myspan"
    val initialSpanBuilder = tracer.buildSpan(name).ignoreActiveSpan
    val taggedSpanBuilder = tagInserter(initialSpanBuilder)
    val span = taggedSpanBuilder.startManual
    span.finish()
    val report = waitForReport
    report.operationName must be (name)
    report.tags must contain ("tag" -> tagComparator)
  }
}
