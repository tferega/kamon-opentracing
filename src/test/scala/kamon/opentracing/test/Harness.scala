package kamon.opentracing
package test

import org.scalatest._

trait Harness extends WordSpec with MustMatchers with BeforeAndAfterEach {
  override def afterEach(): Unit = {
    Option(new Tracer().activeSpan()).foreach(_.deactivate())
    super.afterEach()
  }
}
