package sbtvite

import sbt.io.Path

import scala.util.{Failure, Success, Try}

object ViteConfigGen {
	final case class ValidationError(message: String)

	def generate(
		sourcePaths: List[String],
		rootDirPath: String,
		inputPath: String,
		outDirPath: String,
	): Either[ValidationError, String] =
		for {
			_ <- validateSourcePath(rootDirPath).toLeft(())
			_ <- validateSourcePath(inputPath).toLeft(())
			_ <- validateSourcePath(outDirPath).toLeft(())
			_ <- sourcePaths.foldLeft[Either[ValidationError, Unit]](Right()) { (currentEither, nextPath) =>
				currentEither.flatMap(_ => validateSourcePath(nextPath).toLeft(()))
			}
		} yield combineAll(
			requiredImports ++ generateImportStatements(sourcePaths),
			generateBuildConfigStatements(sourcePaths.length),
			rootDirPath,
			inputPath,
			outDirPath,
		)

	private def validateSourcePath(path: String): Option[ValidationError] =
		Try(java.nio.file.Path.of(path)) match {
			case Failure(exception) =>
				Some(ValidationError(
					s"Invalid configuration source path: ${path}\n${exception}",
				))
			case Success(_) if path.contains(";") =>
				Some(ValidationError(
					"Invalid configuration source path: ${path}\nPath contains ';'",
				))
			case Success(_) if path.contains("\n") =>
				Some(ValidationError(
					"Invalid configuration source path: ${path}\nPath contains new line character",
				))
			case Success(_) if path.contains("'") || path.contains("\"") =>
				Some(ValidationError(
					"Invalid configuration source path: ${path}\nPath contains single or double quote",
				))
			case _ => None
		}

	private val requiredImports = List(
		"""import _ from 'lodash'""",
		"""import { defineConfig } from "vite";""",
		"""import sourcemaps from 'rollup-plugin-sourcemaps';""",
	)

	private val configVariableName = "config"
	private val minimumConfigVariableName = "minimalConfig"
	private val envVariableName = "env"

	private def combineAll(
		importStatements: List[String],
		buildConfigStatements: List[String],
		rootDirPath: String,
		inputPath: String,
		outputDirPath: String,
	): String = {
		val importsString = importStatements.mkString("\n")
		val buildConfigString = buildConfigStatements.mkString("\n  ")

		s"""$importsString
		   |
		   |const $configVariableName = {
		   |  mode: 'development',
		   |  build: {
		   |    cssCodeSplit: false,
		   |    minify: false,
		   |    chunkSizeWarningLimit: 100000,
		   |    reportCompressedSize: false,
		   |    sourcemap: true,
		   |    rollupOptions: {
		   |      treeshake: false,
		   |      output: {
		   |        entryFileNames: `[name].js`,
		   |        chunkFileNames: `[name].js`,
		   |        assetFileNames: `[name].[ext]`,
		   |      },
		   |      plugins: [sourcemaps()],
		   |    },
		   |  },
		   |}
		   |
		   |const $minimumConfigVariableName = {
		   |  root: $rootDirPath,
		   |  build: {
		   |    rollupOptions: {
		   |      input: $inputPath,
		   |      output: {
		   |        dir: $outputDirPath,
		   |      }
		   |    }
		   |  },
		   |}
		   |
		   |export default defineConfig(($envVariableName) => {
		   |  $buildConfigString
		   |
		   |  return $configVariableName;
		   |}
		   |""".stripMargin
	}

	private def configName(i: Int): String = s"config_$i"
	private def appliedConfigName(i: Int): String = configName(i) + "_applied"

	private def generateImportStatements(sourcePaths: List[String]): List[String] = {
		sourcePaths.zipWithIndex.map {
			case (path, i) => s"""import ${configName(i)} from '$path';"""
		}
	}

	private def generateBuildConfigStatements(numConfigs: Int): List[String] = {
		val applyConfigStatements = (0 until numConfigs).toList
		  .map {i =>
			  val conf = configName(i)
			  s"""const ${appliedConfigName(i)} = $conf instanceof Function ? $conf($envVariableName) : $conf;"""
		  }

		val configArray = ((0 until numConfigs).map(appliedConfigName) + minimumConfigVariableName).mkString("[", ", ", "]")

		applyConfigStatements ++ List(
			"""const customizer = (objValue, srcValue) => _.isArray(objValue) && _.isArray(srcValue) ? objValue.concat(srcValue) : undefined;""",
			s"""_.mergeWith($configVariableName, $configArray, customizer);""",
		)
	}
}
