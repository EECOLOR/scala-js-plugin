import sbt.File
import sbt.IO
import com.google.debugging.sourcemap.SourceMapConsumerV3
import com.google.debugging.sourcemap.FilePosition

object SourceMapParser {

  def check(sources: Seq[File], target: File) = {

    // Somehow the last entry is not included in the merged source map
    // this is not in our library, but in the google closure library
    val sourceEntries = sources.flatMap(entriesOf).dropRight(1)

    val targetEntries = entriesOf(target)

    val success = sourceEntries == targetEntries

    if (!success) {
      println("expected:")
      println(sourceEntries)
      println("got:")
      println(targetEntries)
    }

    success
  }

  private def entriesOf(sourceMapFile: File) = {
    var entries = Seq.empty[Entry]
    val consumer = new SourceMapConsumerV3
    consumer.parse(IO.read(sourceMapFile))
    consumer.visitMappings(
      new SourceMapConsumerV3.EntryVisitor {
        override def visit(sourceName: String, symbolName: String, sourceStartPos: FilePosition, startPos: FilePosition, endPos: FilePosition) {
          val s = consumer.getMappingForLine(startPos.getLine + 1, startPos.getColumn + 1)
          entries :+= Entry(s.getOriginalFile, s.getLineNumber, s.getColumnPosition)
        }
      })
    entries
  }

}

case class Entry(source: String, line: Int, column: Int) {
  override def toString = source.split("/").last + " " + line + ":" + column
}