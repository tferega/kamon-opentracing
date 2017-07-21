package kamon.opentracing
package test

import kamon.opentracing.test.TestReporter.Report
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout

import scala.concurrent.duration.DurationDouble

trait Harness extends WordSpec with MustMatchers with BeforeAndAfterEach with Eventually {
  override def afterEach(): Unit = {
    Option(new Tracer().activeSpan()).foreach(_.deactivate())
    super.afterEach()
  }

  protected def waitForReport: Report = eventually(Timeout(2 seconds))(TestReporter.getLatestReport.get)
}
