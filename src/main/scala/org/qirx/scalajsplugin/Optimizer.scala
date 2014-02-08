package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._
import java.io.File
import com.google.javascript.jscomp.SourceFile
import com.google.javascript.jscomp.CompilerOptions
import com.google.javascript.jscomp.CompilationLevel
import com.google.javascript.jscomp.Compiler
import scala.collection.JavaConverters._
import com.google.javascript.jscomp.SourceMap
import java.io.PrintWriter
import java.io.StringWriter
import com.google.debugging.sourcemap.SourceMapConsumerV3
import com.google.debugging.sourcemap.FilePosition
import com.google.debugging.sourcemap.SourceMapGeneratorFactory
import com.google.debugging.sourcemap.SourceMapFormat

object Optimizer {

  //TODO fix naming of the different files
  def optimize(
    sourceFiles: Seq[File], targetName: String,
    targetDirectory: File, cacheDir: File, log: Logger): (File, File) = {

    val (jsFile, jsSourceMapFile) =
      GeneratedFiles.concat(
        sourceFiles,
        targetName = targetName + ".concat",
        targetDirectory = cacheDir, cacheDir = cacheDir / "concat")

    optimize(
      jsFile,
      jsSourceMapFile,
      targetName,
      targetDirectory,
      cacheDir, log)
  }

  private def optimize(
    sourceFile: File, sourceMapFile: File, targetName: String,
    targetDirectory: File, cacheDir: File, log: Logger): (File, File) = {

    val jsFile = targetDirectory / (targetName + ".js")
    val jsSourceMapFile = targetDirectory / (targetName + ".js.map")

    // make sure the target directory exists
    IO.createDirectory(targetDirectory)

    cachedOptimize(cacheDir, sourceFile, sourceMapFile, jsFile, jsSourceMapFile, log)

    jsFile -> jsSourceMapFile
  }

  private def cachedOptimize(cacheDir: File, sourceFile: File, sourceMapFile: File, jsFile: File, jsSourceMapFile: File, log: Logger) = {
    val cachedFunction =
      FileFunction.cached(cacheDir, FilesInfo.lastModified, FilesInfo.exists) _

    val cachedOptimizeFunction =
      cachedFunction { _ =>
        optimize(sourceFile, sourceMapFile, jsFile, jsSourceMapFile, log)
        Set(jsFile, jsSourceMapFile)
      }

    cachedOptimizeFunction(Set(sourceFile))
  }

  private def optimize(sourceFile: File, sourceMapFile: File, jsFile: File, jsSourceMapFile: File, log: Logger) = {
    val closureSource = SourceFile.fromFile(sourceFile)

    val options = new CompilerOptions
    options.setSourceMapFormat(SourceMap.Format.V3)
    println("sourceMapOutputPath: " + jsSourceMapFile.getAbsolutePath)
    options.setSourceMapDetailLevel(SourceMap.DetailLevel.ALL)
    options.setSourceMapOutputPath(jsSourceMapFile.getAbsolutePath)
    CompilationLevel.ADVANCED_OPTIMIZATIONS
      .setOptionsForCompilationLevel(options)
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5)

    val compiler = new Compiler
    val result = compiler.compile(
      Seq.empty[SourceFile].asJava,
      Seq(closureSource).asJava, options)

    val errors = result.errors
    if (errors.nonEmpty) errors.foreach(e => log.error(e.toString))
    val warnings = result.warnings
    if (warnings.nonEmpty) warnings.foreach(e => log.warn(e.toString))

    IO.write(jsFile, compiler.toSource())

    processSourceMap(compiler.getSourceMap, jsFile, jsSourceMapFile, sourceFile, sourceMapFile)
  }

  private def processSourceMap(sourceMap: SourceMap, jsFile: File, jsSourceMapFile: File, sourceFile: File, sourceMapFile: File) = {

    val sourceMapGenerator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3)

    val sourceMapWriter = new StringWriter()
    sourceMap.appendTo(sourceMapWriter, jsFile.name)
    val sourceMapContents = sourceMapWriter.toString
    sourceMapWriter.close()

    val compileSourceMapConsumer = new SourceMapConsumerV3
    compileSourceMapConsumer.parse(sourceMapContents)

    val concatSourceMapConsumer = new SourceMapConsumerV3
    concatSourceMapConsumer.parse(IO.read(sourceMapFile))

    val sourceFileName = sourceFile.getAbsolutePath

    compileSourceMapConsumer.visitMappings(new SourceMapConsumerV3.EntryVisitor {
      override def visit(sourceName: String, symbolName: String,
        sourceStartPos: FilePosition,
        startPos: FilePosition, endPos: FilePosition): Unit = {

        assert(sourceName == sourceFileName, s"Invalid sourceName, expected:\n$sourceFileName\ngot:\n$sourceName")

        val originalMapping =
          concatSourceMapConsumer.getMappingForLine(sourceStartPos.getLine + 1, sourceStartPos.getColumn + 1)

        if (originalMapping != null) {

          val newSourceStartPos =
            new FilePosition(originalMapping.getLineNumber - 1, originalMapping.getColumnPosition - 1)

          val newSourceName = originalMapping.getOriginalFile

          sourceMapGenerator.addMapping(
            newSourceName, symbolName, newSourceStartPos, startPos, endPos)
        }
      }
    })

    val jsSourceMapOut = new PrintWriter(jsSourceMapFile)
    sourceMapGenerator.appendTo(jsSourceMapOut, jsFile.name)
    jsSourceMapOut.close()
  }
}