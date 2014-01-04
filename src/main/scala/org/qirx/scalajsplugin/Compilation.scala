// format: +preserveDanglingCloseParenthesis

package org.qirx.scalajsplugin

import sbt._
import sbt.Keys._

trait Compilation {

  lazy val compilationSettings = Seq(

    addCompilerPlugin("org.scala-lang.modules.scalajs" %% "scalajs-compiler" % "0.2-SNAPSHOT"),

    incOptions ~= { incOptions =>
      // make sbt incremental compilation compiler aware of the generated javascript files
      val scalaJSClassfileManager = ScalaJSClassfileManager(incOptions.newClassfileManager)
      incOptions.copy(newClassfileManager = scalaJSClassfileManager)
    }
  )
}