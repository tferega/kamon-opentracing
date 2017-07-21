val kamonCore       = "io.kamon"         %% "kamon-core"       % "1.0.0-RC1-5c8a8d169858b83a059c89e48cb43a41040788b8"
val opentracingApi  = "io.opentracing"   %  "opentracing-api"  % "0.30.0"

name := "kamon-opentracing"
libraryDependencies ++=
  compileScope(kamonCore, opentracingApi) ++
  testScope(logbackClassic, scalatest)
