import org.scalajs.linker.interface.ModuleSplitStyle

version := "0.1"
scalaVersion := "3.3.1"

libraryDependencies += "com.lihaoyi" %%% "utest" % "0.8.2" % Test
libraryDependencies ++= Seq(
	"io.github.cquiroz" %%% "scala-java-time" % "2.5.0" % Test,
	"io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.5.0" % Test,
)
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1"
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"

enablePlugins(ScalaJSPlugin, SbtVitePlugin)

scalaJSLinkerConfig ~= {
	_.withModuleKind(ModuleKind.ESModule)
	 .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("gsp")))
}

viteOtherSources += Location.FromProject(file("src/main/javascript"))
viteOtherSources += Location.FromProject(file("src/main/entrypoint"))
viteOtherSources += Location.FromProject(file("src/main/styles"))

testFrameworks += new TestFramework("utest.runner.Framework")

lazy val installDeps = taskKey[Unit]("Install npm dependencies on startup")

installDeps := {
	import scala.sys.process.*
	val rootFile = file(".")
	Process("rm -rf node_modules", Some(rootFile)).run().exitValue()
	Process("npm install", Some(rootFile)).run().exitValue()
}

lazy val startupTransition: State => State = { s: State =>
	"installDeps" :: s
}

Global / onLoad := {
	val old = (Global / onLoad).value
	// compose the new transition on top of the existing one
	// in case your plugins are using this hook.
	startupTransition compose old
}

viteDependencyManagement := DependencyManagement.Manual
