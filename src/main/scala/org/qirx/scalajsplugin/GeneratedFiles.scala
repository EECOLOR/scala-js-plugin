/* Scala.js sbt plugin
 * Copyright 2013 LAMP/EPFL
 * @author  Sébastien Doeraene
 */

package org.qirx.scalajsplugin

import java.io.PrintWriter
import com.google.debugging.sourcemap.FilePosition
import com.google.debugging.sourcemap.SourceMapConsumerV3
import com.google.debugging.sourcemap.SourceMapFormat
import com.google.debugging.sourcemap.SourceMapGeneratorFactory
import sbt.File
import sbt.IO
import sbt.richFile
import sbt.FileFunction
import sbt.FilesInfo
import com.google.debugging.sourcemap.SourceMapGenerator
import com.google.javascript.jscomp.SourceMap
import java.io.StringWriter

object GeneratedFiles {

  def concat(
    sourceFiles: Seq[File], targetName: String,
    targetDirectory: File, cacheDir: File): (File, File) = {

    // make sure the target directory exists
    IO.createDirectory(targetDirectory)

    val jsFile = targetDirectory / (targetName + ".js")
    val jsSourceMapFile = targetDirectory / (targetName + ".js.map")

    cachedConcat(cacheDir, sourceFiles, jsFile, jsSourceMapFile)

    jsFile -> jsSourceMapFile
  }

  private def cachedConcat(cacheDir: File, sourceFiles: Seq[File], jsFile: File, jsSourceMapFile: File) = {
    val cachedFunction =
      FileFunction.cached(cacheDir, FilesInfo.lastModified, FilesInfo.exists) _

    val cachedConcatFunction =
      cachedFunction { _ /* do not use these files, they are not sorted */ =>
        concat(sourceFiles, jsFile, jsSourceMapFile)
        Set(jsFile, jsSourceMapFile)
      }

    cachedConcatFunction(sourceFiles.toSet)
  }

  private def concat(sourceFiles: Seq[File], jsFile: File, jsSourceMapFile: File): Unit = {

    val jsOut = new PrintWriter(jsFile)
    val sourceMapGenerator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3)

    var totalLineCount = 0
    for (sourceFile <- sourceFiles) yield {
      val offset = totalLineCount

      val lineCount = processSourceFile(sourceFile, jsOut)

      val sourceMapFile = sourceMapOf(sourceFile)
      if (sourceMapFile.exists)
        processSourceMap(sourceMapFile, sourceMapGenerator, offset)
      else
        addFakeSourceMap(sourceFile, sourceMapGenerator, offset, lineCount)

      totalLineCount += lineCount
    }

    jsOut.println("//@ sourceMappingURL=" + jsSourceMapFile.getName)
    jsOut.close()

    val jsSourceMapOut = new PrintWriter(jsSourceMapFile)
    sourceMapGenerator.appendTo(jsSourceMapOut, jsFile.name)
    jsSourceMapOut.close()
  }

  private def sourceMapOf(jsfile: File): File =
    jsfile.getParentFile / (jsfile.getName + ".map")

  private def processSourceFile(sourceFile: File, jsOut: PrintWriter) = {

    val lines = IO.readLines(sourceFile)
    val lineCount = lines.size

    // Cat the file - remove references to source maps
    for (line <- lines) {
      if (line startsWith "//@ sourceMappingURL=") jsOut.println()
      else jsOut.println(line)
    }

    lineCount
  }

  private def processSourceMap(sourceMapFile: File, sourceMapGenerator: SourceMapGenerator, offset: Int) = {

    /* The source map exists.
       * Visit all the mappings in this source map, and add them to the
       * concatenated source map with the appropriate offset.
       */
    val consumer = new SourceMapConsumerV3
    consumer.parse(IO.read(sourceMapFile))

    consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor {
      override def visit(sourceName: String, symbolName: String,
        sourceStartPos: FilePosition,
        startPos: FilePosition, endPos: FilePosition) {

        val offsetStartPos =
          new FilePosition(startPos.getLine + offset, startPos.getColumn)
        val offsetEndPos =
          new FilePosition(endPos.getLine + offset, endPos.getColumn)

        sourceMapGenerator.addMapping(sourceName, symbolName,
          sourceStartPos, offsetStartPos, offsetEndPos)
      }
    })
  }

  private def addFakeSourceMap(sourceFile: File, sourceMapGenerator: SourceMapGenerator, offset: Int, lineCount: Int) = {
    /* The source map does not exist.
     * This happens typically for corejslib.js and other helper files
     * written directly in JS.
     * We generate a fake line-by-line source map for these on the fly
     */
    sys.error("addFakeSourceMap disabled in GeneratedFiles")
    val sourceName = sourceFile.getPath
    for (lineNumber <- 0 until lineCount) {
      val sourceStartPos = new FilePosition(lineNumber, 0)
      val startPos = new FilePosition(offset + lineNumber, 0)
      val endPos = new FilePosition(offset + lineNumber + 1, 0)

      sourceMapGenerator.addMapping(sourceName, null,
        sourceStartPos, startPos, endPos)
    }
  }
}
