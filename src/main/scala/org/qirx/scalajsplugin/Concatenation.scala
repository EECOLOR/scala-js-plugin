// format: +preserveDanglingCloseParenthesis

package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._

trait Concatenation {

  import ScalaJSPlugin.ScalaJSKeys.concatenateGeneratedFiles
  import ScalaJSPlugin.ScalaJSKeys.concatenateFiles

  lazy val concatenationSettings = concatenationSettingsIn(Compile)

  def concatenationSettingsIn(configuration: Configuration) =
    inConfig(configuration)(baseConcatenationSettings)

  lazy val baseConcatenationSettings = Seq(

    concatenateGeneratedFiles in resourceGenerators := true,

    sources in concatenateFiles := {
      val compiled = compile.value
      val sourceDirectory = classDirectory.value
      val sources = sourceDirectory ** GeneratedFile.jsFilter
      val result = sources.get.sortBy(_.name)
      result
    },

    target in concatenateFiles := resourceManaged.value,

    concatenateFiles := {
      val sourceFiles = (sources in concatenateFiles).value
      val concat = concatenateFilesTask.value
      concat(sourceFiles)
    },

    resourceGenerators <+=
      Def.taskDyn {
        if ((concatenateGeneratedFiles in resourceGenerators).value)
          concatenateFiles.map {
            case (jsFile, jsSourceMapFile) => Seq(jsFile, jsSourceMapFile)
          }
        else Def.task { Seq.empty[File] }
      }
  )

  def concatenateFilesTask =
    Def.task { sourceFiles: Seq[File] =>
      GeneratedFiles.concat(
        sourceFiles,
        targetName = moduleName.value,
        targetDirectory = (target in concatenateFiles).value,
        cacheDir = streams.value.cacheDirectory / "concatenate-generated-files")
    }

}