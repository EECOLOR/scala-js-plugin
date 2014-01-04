package org.qirx.scalajsplugin

import java.util.regex.Pattern
import sbt.PatternFilter

object GeneratedFile {

  private val prefix = raw"\d{4}-"
  private val jsSuffix = raw"\.js"
  private val suffix = jsSuffix + raw"(?:\.map)?"
  private val fileName = raw"[^\.]+"

  val pattern = prefix + fileName + suffix
  val jsPattern = prefix + fileName + jsSuffix
  val jsFilter = new PatternFilter(Pattern.compile(jsPattern))

  def pattern(baseName: String) = {
    val escapedBaseName = Pattern.quote(baseName)
    prefix + escapedBaseName + suffix
  }
}