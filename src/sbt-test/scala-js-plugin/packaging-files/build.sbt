scalaJSSettings

TaskKey[Unit]("extract-jar") := {
  val jar = (packageBin in Compile).value
  IO.unzip(jar, (target in Compile).value / "extracted")
}
