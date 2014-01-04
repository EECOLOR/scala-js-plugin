scalaJSSettings

val classDir = taskKey[File]("")

classDir := (classDirectory in Compile).value

val resourceDir = taskKey[File]("")

resourceDir := {
  val generated = (managedResources in Compile).value
  (resourceManaged in Compile).value
}

TaskKey[Unit]("check-content-of-javascript-file") := {
  val javascriptFile = IO.read(resourceDir.value / "concatenating-files.js").replaceAll("//@ source.+", "")
  val file1 = IO.read(classDir.value / "0002-Test1$.js").replaceAll("//@ source.+", "")
  val file2 = IO.read(classDir.value / "0002-Test2.js").replaceAll("//@ source.+", "")
  val expected = file1 + file2 + "\n"
  val concatSuccess = javascriptFile == expected
  if (!concatSuccess) {
    println("expected: \n" + expected)
    println("got: \n" + javascriptFile)
  }
  assert(concatSuccess, "Concatenation of source files failed")
}

TaskKey[Unit]("check-content-of-sourcemap-file") := {
  val sourceMapFile = resourceDir.value / "concatenating-files.js.map"
  val concatSuccess =
    SourceMapParser.check(
      sources = Seq(classDir.value / "0002-Test1$.js.map", classDir.value / "0002-Test2.js.map"),
      target = sourceMapFile)
  assert(concatSuccess, "Concatenation of source maps failed")
}