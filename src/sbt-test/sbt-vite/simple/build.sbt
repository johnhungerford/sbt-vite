import org.scalajs.linker.interface.ModuleSplitStyle

version := "0.1"
scalaVersion := "3.3.1"

libraryDependencies += "com.lihaoyi" %%% "utest" % "0.8.2" % Test
libraryDependencies ++= Seq(
	"io.github.cquiroz" %%% "scala-java-time" % "2.5.0" % Test,
	"io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.5.0" % Test,
)

enablePlugins(ScalaJSPlugin, SbtVitePlugin)

scalaJSLinkerConfig ~= {
	_.withModuleKind(ModuleKind.ESModule)
	 .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("gsp")))
}

testFrameworks += new TestFramework("utest.runner.Framework")