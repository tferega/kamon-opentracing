val kamonCore       = "io.kamon"         %% "kamon-core"       % "1.0.0-RC1-483159862293a065be7cf3743d1aa759fbf31fc0"
val opentracingApi  = "io.opentracing"   %  "opentracing-api"  % "0.30.0"

name := "kamon-opentracing"
libraryDependencies ++=
  compileScope(kamonCore, opentracingApi) ++
  testScope(scalatest)
