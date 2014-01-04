// format: +preserveDanglingCloseParenthesis

package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._

trait Packaging {

  import ScalaJSPlugin.ScalaJSKeys.excludeGeneratedFiles

  lazy val packagingSettings =
    packagingSettingsIn(Compile)

  def packagingSettingsIn(configuration: Configuration) =
    inConfig(configuration)(basePackagingSettings)

  lazy val basePackagingSettings = Seq(

    excludeGeneratedFiles in packageBin := true,

    mappings in packageBin <<=
      (mappings in packageBin, excludeGeneratedFiles in packageBin).map {
        (mappings, excludeGeneratedFiles) =>
          if (excludeGeneratedFiles)
            mappings.filterNot {
              case (file, _) => file.name matches GeneratedFile.pattern
            }
          else mappings
      }
  )
}