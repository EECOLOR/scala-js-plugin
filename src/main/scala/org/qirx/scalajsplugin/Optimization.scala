// format: +preserveDanglingCloseParenthesis

package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._

trait Optimization {

  import ScalaJSPlugin.ScalaJSKeys.optimize
  import ScalaJSPlugin.ScalaJSKeys.optimizeFiles
  import ScalaJSPlugin.ScalaJSKeys.concatenateFiles
  import ScalaJSPlugin.concatenateFilesTask

  lazy val optimizationSettings = optimizationSettingsIn(Compile)

  def optimizationSettingsIn(configuration: Configuration) =
    inConfig(configuration)(baseOptimizationSettings)

  lazy val baseOptimizationSettings = Seq(

    optimize in resourceGenerators := false,

    sources in optimizeFiles := {
      val result = concatenateFiles.value
      val (jsFile, _) = result
      Seq(jsFile)
    },

    optimizeFiles := {
      val sourceFiles = (sources in optimizeFiles).value
      val concat = optimizeFilesTask.value
      concat(sourceFiles)
    },

    resourceGenerators <+=
      Def.taskDyn {
        if ((optimize in resourceGenerators).value)
          optimizeFiles.map {
            case (jsFile, jsSourceMapFile) => Seq(jsFile, jsSourceMapFile)
          }
        else Def.task { Seq.empty[File] }
      }
  )

  def optimizeFilesTask =
    Def.task { sourceFiles: Seq[File] =>
      val concat = concatenateFilesTask.value
      val (jsFile, jsSourceMapFile) = concat(sourceFiles)
      Optimize(
        jsFile,
        jsSourceMapFile,
        targetName = moduleName.value + ".min",
        targetDirectory = (target in optimizeFiles).value,
        cacheDir = streams.value.cacheDirectory / "optimize-files")
    }

}