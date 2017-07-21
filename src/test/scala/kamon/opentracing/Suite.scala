package kamon.opentracing

import kamon.Kamon
import kamon.opentracing.test.{Harness, TestReporter}
import org.scalatest.{BeforeAndAfterAll, Suites}

object Suite {
  private def suiteList: Seq[Harness] = Seq(
    new TracerSpec,
    new SpanBuilderSpec
  )
}

class Suite extends Suites(Suite.suiteList: _*) with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    super.beforeAll()
    Kamon.addReporter(TestReporter)
  }
}
