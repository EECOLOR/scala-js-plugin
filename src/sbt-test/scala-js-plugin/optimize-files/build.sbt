scalaJSSettings

ScalaJSKeys.optimize in (Compile, resourceGenerators) := true

TaskKey[Unit]("check-content-of-javascript-file") := {
}

TaskKey[Unit]("check-content-of-sourcemap-file") := {
}
