inThisBuild(List(
  scalaVersion := "2.12.17",
  sbtPlugin := true,
  organization := "io.github.johnhungerford.sbt.vite",
  organizationName := "johnhungerford",
  organizationHomepage := Some(url("https://johnhungerford.github.io")),
  homepage := Some(url("https://johnhungerford.github.io")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  startYear := Some(2024),
  developers := List(
    Developer(
      id    = "johnhungerford",
      name  = "John Hungerford",
      email = "jiveshungerford@gmail.com",
      url   = url( "https://johnhungerford.github.io" )
    )
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/johnhungerford/sbt-vite"),
      "scm:git@github.com:johnhungerford/sbt-vite.git"
    )
  ),
//  githubWorkflowTargetTags ++= Seq("v*"),
//  githubWorkflowPublishTargetBranches :=
//    Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
  scriptedLaunchOpts ++=
    Seq("-Xmx1024M", "-Dvite.plugin.version=" + version.value),
))

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.15.0")

console / initialCommands := """import sbtvite._"""
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeProfileName := "io.github.johnhungerford"

enablePlugins(ScriptedPlugin)
