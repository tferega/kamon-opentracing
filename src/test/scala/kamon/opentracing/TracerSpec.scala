package kamon.opentracing

import java.nio.ByteBuffer

import io.opentracing.propagation.{Format => OpenFormat}
import kamon.opentracing.test.{Harness, TestTextMap}
import kamon.trace.ActiveSpan.{Default => KamonDefaultActiveSpan}
import kamon.trace.IdentityProvider.{Identifier => KamonIdentifier}
import kamon.trace.SpanContext.{Baggage => KamonBaggage, SamplingDecision => KamonSamplingDecision, Source => KamonSource}
import kamon.trace.{SpanContext => KamonSpanContext, SpanContextCodec => KamonSpanContextCodec}
import kamon.util.HexCodec
import org.scalatest.DoNotDiscover

@DoNotDiscover
class TracerSpec extends Harness {
  var tracer: Tracer = _

  private def initId(id: Long): KamonIdentifier = {
    val data = ByteBuffer.wrap(new Array[Byte](8))
    data.putLong(id)
    KamonIdentifier(HexCodec.toLowerHex(id), data.array())
  }

  "Tracer" should {
    "be available via a no-arg constructor" in {
      tracer = new Tracer
      // Quack
    }

    "be able to provide a SpanBuilder" in {
      val builder = tracer.buildSpan("myspan")
      builder must be (a[SpanBuilder])
    }

    "extract a SpanContext from a HttpHeader" in {
      import KamonSpanContextCodec.ExtendedB3.Headers._
      val traceId  = initId(1).string
      val parentId = initId(2).string
      val spanId   = initId(3).string
      val carrier = new TestTextMap(
        TraceIdentifier -> traceId,
        ParentSpanIdentifier -> parentId,
        SpanIdentifier -> spanId,
        Sampled -> "1")
      val context = tracer.extract(OpenFormat.Builtin.HTTP_HEADERS, carrier)
      context must be (a[SpanContext])
      val wrappedContext = context.asInstanceOf[SpanContext].unwrap
      wrappedContext.traceID.string must be (traceId)
      wrappedContext.parentID.string must be (parentId)
      wrappedContext.spanID.string must be (spanId)
      wrappedContext.samplingDecision must be (KamonSamplingDecision.Sample)
    }

    "extract a SpanContext from a TextMap" in {
      import KamonSpanContextCodec.ExtendedB3.Headers._
      val traceId  = initId(1).string
      val spanId   = initId(2).string
      val parentId = initId(3).string
      val carrier = new TestTextMap(
        TraceIdentifier -> traceId,
        SpanIdentifier -> spanId,
        ParentSpanIdentifier -> parentId,
        Sampled -> "1")
      val context = tracer.extract(OpenFormat.Builtin.TEXT_MAP, carrier)
      context must be (a[SpanContext])
      val wrappedContext = context.asInstanceOf[SpanContext].unwrap
      wrappedContext.traceID.string must be (traceId)
      wrappedContext.parentID.string must be (parentId)
      wrappedContext.spanID.string must be (spanId)
      wrappedContext.samplingDecision must be (KamonSamplingDecision.Sample)
    }

    "inject a SpanContext into a HttpHeader" in {
      import KamonSpanContextCodec.ExtendedB3.Headers._
      val traceId  = initId(1)
      val parentId = initId(2)
      val spanId   = initId(3)
      val wrappedContext = KamonSpanContext(traceId, spanId, parentId, KamonSamplingDecision.Sample, KamonBaggage(), KamonSource.Local)
      val context = SpanContext.wrap(wrappedContext)
      val carrier = new TestTextMap
      tracer.inject(context, OpenFormat.Builtin.HTTP_HEADERS, carrier)
      carrier.get(TraceIdentifier) must be (Some(traceId.string))
      carrier.get(SpanIdentifier) must be (Some(spanId.string))
      carrier.get(ParentSpanIdentifier) must be (Some(parentId.string))
      carrier.get(Sampled) must be (Some("1"))
    }

    "inject a SpanContext into a TextMap" in {
      import KamonSpanContextCodec.ExtendedB3.Headers._
      val traceId  = initId(1)
      val parentId = initId(2)
      val spanId   = initId(3)
      val wrappedContext = KamonSpanContext(traceId, spanId, parentId, KamonSamplingDecision.Sample, KamonBaggage(), KamonSource.Local)
      val context = SpanContext.wrap(wrappedContext)
      val carrier = new TestTextMap
      tracer.inject(context, OpenFormat.Builtin.TEXT_MAP, carrier)
      carrier.get(TraceIdentifier) must be (Some(traceId.string))
      carrier.get(SpanIdentifier) must be (Some(spanId.string))
      carrier.get(ParentSpanIdentifier) must be (Some(parentId.string))
      carrier.get(Sampled) must be (Some("1"))
    }

    "get an active span when there are none" in {
      val span = tracer.activeSpan()
      span must be (null)
    }

    "get an active span where there is one" in {
      val span = tracer.buildSpan("myspan").ignoreActiveSpan.startActive
      span must be (a[ActiveSpan])
      val activeSpan = span.asInstanceOf[ActiveSpan]
      activeSpan.unwrap must be (a[KamonDefaultActiveSpan])
      val wrappedSpan = activeSpan.unwrap.asInstanceOf[KamonDefaultActiveSpan]
      wrappedSpan.context.traceID.string mustNot be ("")
    }
  }
}
