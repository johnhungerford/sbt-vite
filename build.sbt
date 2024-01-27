import sbt.file

import scala.util.Try

lazy val setPluginVersion = taskKey[Unit]("Set plugin version for scripted tests")

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
  scriptedLaunchOpts ++=
    Seq("-Xmx1024M", "-Dvite.plugin.version=" + version.value),
))
setPluginVersion := {
  IO.listFiles(file("src/sbt-test/sbt-vite")).toList.foreach(file => {
    println(file)
    if (file.isDirectory) {
      println
      val pluginsSbt =
        s"""{
		  |  addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.15.0")
		  |  addSbtPlugin("io.github.johnhungerford.sbt.vite" % "sbt-vite" % "${version.value}")
          |}""".stripMargin
      Try(IO.write(file / "project" / "plugins.sbt", pluginsSbt))
      println(IO.read(file / "project" / "plugins.sbt"))
    }
  })
}
sbtLauncher := sbtLauncher.dependsOn(setPluginVersion).value
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.15.0")

console / initialCommands := """import sbtvite._"""
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeProfileName := "io.github.johnhungerford"

enablePlugins(ScriptedPlugin)
