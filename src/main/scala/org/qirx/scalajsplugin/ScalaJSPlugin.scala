// format: +preserveDanglingCloseParenthesis
package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._

object ScalaJSPlugin extends Plugin
  with Compilation with Concatenation with Packaging with Optimization {

  object ScalaJSKeys {
    val concatenateFiles = TaskKey[(File, File)]("concatenate-files", "Concatenates both generated files and their source maps. Returns the javascript file and the sourcemap file")
    val concatenateGeneratedFiles = TaskKey[Boolean]("concatenate-generated-files", "Allows you to turn off the concatenation of files")
    val excludeGeneratedFiles = SettingKey[Boolean]("exclude-generated-files", "If set to false will include generated files. For example used during packaging")
    val optimize = SettingKey[Boolean]("optimize", "Enables generating an optimized version of the concatenated files")
    val optimizeFiles = TaskKey[(File, File)]("optimize-files", "Optimizes sources in optimizeFiles. Returns the javascript file and the sourcemap file")
  }

  lazy val scalaJSSettings =
    scalaJSResolvers ++
      compilationSettings ++
      concatenationSettings ++
      packagingSettings ++
      optimizationSettings

  private def scalaJSResolver(repo: String) = Resolver.url(s"scala-js-$repo",
    url(s"http://repo.scala-js.org/repo/$repo/"))(Resolver.ivyStylePatterns)

  lazy val scalaJSResolvers = Seq(
    resolvers ++= Seq(scalaJSResolver("releases"), scalaJSResolver("snapshots"))
  )
}