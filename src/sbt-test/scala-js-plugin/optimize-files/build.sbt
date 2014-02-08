scalaJSSettings

ScalaJSKeys.optimize in (Compile, resourceGenerators) := true

val resourceDir = TaskKey[File]("resource-dir")

resourceDir := (resourceManaged in Compile).value

TaskKey[Unit]("check-content-of-javascript-file") := {
  val content = IO.read(resourceDir.value / "optimize-files.min.js")
  assert(!content.matches("\\s*"), "The file is empty")
  // I think checking the actual content should be covered in the integration tests
}

TaskKey[Unit]("check-content-of-sourcemap-file") := {
  val classDir = (classDirectory in Compile).value
  val content = IO.read(resourceDir.value / "optimize-files.min.js.map")
  assert(!content.matches("\\s*"), "The file is empty")
  val sourceMapFile = resourceDir.value / "optimize-files.min.js.map"
  val concatSuccess =
    SourceMapParser.check(
      sources = Seq(classDir / "0002-Test1$.js.map", classDir / "0002-Test2.js.map"),
      target = sourceMapFile)
  assert(concatSuccess, "Concatenation of source maps failed")
}
