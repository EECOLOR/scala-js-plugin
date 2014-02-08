// format: +preserveDanglingCloseParenthesis

package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._

trait Optimization {

  import ScalaJSPlugin.ScalaJSKeys.optimize
  import ScalaJSPlugin.ScalaJSKeys.optimizeFiles
  import ScalaJSPlugin.ScalaJSKeys.concatenateFiles

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

    target in optimizeFiles := resourceManaged.value,

    optimizeFiles := {
      val sourceFiles = (sources in optimizeFiles).value
      val s = streams.value
      Optimizer.optimize(
        sourceFiles,
        targetName = moduleName.value + ".min",
        targetDirectory = (target in optimizeFiles).value,
        cacheDir = s.cacheDirectory / "optimize-files",
        s.log)
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
}