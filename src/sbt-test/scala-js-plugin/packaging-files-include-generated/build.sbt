scalaJSSettings

ScalaJSKeys.excludeGeneratedFiles in (Compile, packageBin) := false

TaskKey[Unit]("extract-jar") := {
  val jar = (packageBin in Compile).value
  IO.unzip(jar, (target in Compile).value / "extracted")
}
