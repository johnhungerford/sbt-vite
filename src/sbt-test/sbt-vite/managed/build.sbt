import org.scalajs.linker.interface.ModuleSplitStyle

version := "0.1"
scalaVersion := "3.3.1"

libraryDependencies += "com.lihaoyi" %%% "utest" % "0.8.2" % Test
libraryDependencies ++= Seq(
	"io.github.cquiroz" %%% "scala-java-time" % "2.5.0" % Test,
	"io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.5.0" % Test,
)
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1"

enablePlugins(ScalaJSPlugin, SbtVitePlugin)

scalaJSLinkerConfig ~= {
	_.withModuleKind(ModuleKind.ESModule)
	 .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("gsp")))
}

viteOtherSources ++= Seq(
	Location.FromProject(file("src/main/javascript")),
	Location.FromProject(file("src/main/entrypoint")),
	Location.FromProject(file("src/main/styles")),
)

npmDependencies ++= Seq(
	"react" -> "^18.2.0",
	"react-dom" -> "^18.2.0",
	"prop-types" -> "^15.8.1",
	"react-toastify" -> "^6.0.8",
)

testFrameworks += new TestFramework("utest.runner.Framework")

