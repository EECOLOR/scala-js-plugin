package org.qirx.scalajsplugin

import sbt.inc.ClassfileManager
import java.io.File
import java.util.regex.Pattern
import sbt._

private class ScalaJSClassfileManager(inherited: ClassfileManager) extends ClassfileManager {

  def delete(classes: Iterable[File]): Unit = {
    val allFiles = addGeneratedFilesFor(classes)
    inherited.delete(allFiles)
  }

  def generated(classes: Iterable[File]): Unit = inherited.generated(classes)
  def complete(success: Boolean): Unit = inherited.complete(success)

  private def addGeneratedFilesFor(classes: Iterable[File]) =
    classes flatMap { classFile =>
      classFile :: getGeneratedFilesFor(classFile)
    }

  private def getGeneratedFilesFor(classFile: File) = {
    val classFileName = classFile.name

    if (classFileName endsWith ".class") {
      val baseName = classFileName.substring(0, classFileName.length - 6)
      val pattern = GeneratedFile.pattern(baseName)

      val possibleSiblings = Option(classFile.getParentFile.listFiles)
      for {
        siblings <- possibleSiblings.toList
        file <- siblings if (file.name matches pattern)
      } yield file
    } else Nil
  }
}

object ScalaJSClassfileManager {
  def apply(inherited: () => ClassfileManager): () => ClassfileManager =
    () => new ScalaJSClassfileManager(inherited())
}