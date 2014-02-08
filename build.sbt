scriptedSettings

name := "scala-js-plugin"

organization := "org.qirx"

sbtPlugin := true

// needed to find the sbt-launch dependency (added by the scripted plugin
resolvers += Resolver.url("Typesafe repository", url("http://repo.typesafe.com/typesafe/releases/"))(Resolver.ivyStylePatterns)

libraryDependencies += "com.google.javascript" % "closure-compiler" % "v20131014"

scriptedBufferLog := false
