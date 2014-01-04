package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._
import java.io.File

object Optimize {
  def apply(
    sourceFile: File, sourceMapFile:File, targetName: String,
    targetDirectory: File, cacheDir: File): (File, File) = {

    val jsFile = targetDirectory / (targetName + ".js")
    val jsSourceMapFile = targetDirectory / (targetName + ".js.map")

    println("writing file: " + jsFile)
    jsFile.createNewFile()
    jsSourceMapFile.createNewFile()

    (jsFile, jsSourceMapFile)
  }
}