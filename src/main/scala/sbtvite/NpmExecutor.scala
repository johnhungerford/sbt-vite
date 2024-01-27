package sbtvite

import sbtvite.config.{DependencyManagement, NpmManager}

import scala.sys.process.*

trait NpmExecutor {
	def install(
		deps: Map[String, String],
		devDeps: Map[String, String],
		additionalOptions: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit]

	def run(
		pckg: String,
		module: String,
		additionalOptions: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit]
}

object NpmExecutor {
	private[sbtvite] def depString(tup: (String, String)): String = {
		s"${tup._1}@${tup._2}"
	}

	private[sbtvite] def depsString(deps: Seq[(String, String)]): String = {
		deps.map(depString).mkString(" ")
	}

	def apply(manager: NpmManager): NpmExecutor = manager match {
		case NpmManager.Npm => NpmNpmExecutor
		case NpmManager.Yarn => YarnNpmExecutor
		case NpmManager.Pnpm => PnpmNpmExecutor
	}
}

object NpmNpmExecutor extends NpmExecutor {
	def install(
		deps: Map[String, String],
		devDeps: Map[String, String],
		additionalOptions: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit] = {
		val additionalOptionsStr = additionalOptions.mkString(" ")
		val baseCommand =
			s"npm install $additionalOptionsStr"
		val existingCommand = baseCommand
		val command = baseCommand + " " + NpmExecutor.depsString(deps.toSeq)
		val devCommand = baseCommand + " --save-dev " + NpmExecutor.depsString(devDeps.toSeq)

		cwd.foreach(sbt.IO.createDirectory)
		val existingResult = Process(existingCommand, cwd, environment.toSeq*).run().exitValue()
		val result = Process(command, cwd, environment.toSeq*).run().exitValue()
		val devResult = Process(devCommand, cwd, environment.toSeq*).run().exitValue()

		for {
			_ <- if (existingResult == 0) Right(()) else Left(s"Failed to install npm dependencies using npm. Exit code: $result")
			_ <- if (result == 0) Right(()) else Left(s"Failed to install npm dependencies using npm. Exit code: $result")
			_ <- if (devResult == 0) Right(()) else Left(s"Failed to install npm dev dependencies using npm. Exit code: $devResult")
		} yield ()
	}

	def run(
		pckg: String,
		module: String,
		arguments: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit] = {
		val baseCommand = s"npx --no -p $pckg $module"

		val command = s"$baseCommand ${arguments.mkString(" ")}"

		cwd.foreach(sbt.IO.createDirectory)
		val result = Process(command, cwd, environment.toSeq*).run().exitValue()

		if (result == 0) Right(())
		else Left(s"Failed to run $module in npm package $pckg using npx. Exit code: $result")
	}
}

object YarnNpmExecutor extends NpmExecutor {
	def install(
		deps: Map[String, String],
		devDeps: Map[String, String],
		additionalOptions: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit] = {
		val additionalOptionsStr = additionalOptions.mkString(" ")
		val baseCommand = s"yarn add $additionalOptionsStr"
		val command = baseCommand + " " + NpmExecutor.depsString(deps.toSeq)
		val devCommand = baseCommand + " --dev " + NpmExecutor.depsString(devDeps.toSeq)

		cwd.foreach(sbt.IO.createDirectory)
		val existingResult = Process(baseCommand, cwd, environment.toSeq*).run().exitValue()
		val result = Process(command, cwd, environment.toSeq*).run().exitValue()
		val devResult = Process(devCommand, cwd, environment.toSeq*).run().exitValue()

		for {
			_ <- if (existingResult == 0) Right(()) else Left(s"Failed to install npm dependencies using npm. Exit code: $result")
			_ <- if (result == 0) Right(()) else Left(s"Failed to install npm dependencies using npm. Exit code: $result")
			_ <- if (devResult == 0) Right(()) else Left(s"Failed to install npm dev dependencies using npm. Exit code: $devResult")
		} yield ()
	}

	def run(
		pckg: String,
		module: String,
		arguments: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit] = {
		val baseCommand = s"npx --no -p $pckg $module"

		val command = s"$baseCommand ${arguments.mkString(" ")}"

		cwd.foreach(sbt.IO.createDirectory)
		val result = Process(command, cwd, environment.toSeq*).run().exitValue()

		if (result == 0) Right(())
		else Left(s"Failed to run $module in npm package $pckg using npx. Exit code: $result")
	}
}

object PnpmNpmExecutor extends NpmExecutor {
	def install(
		deps: Map[String, String],
		devDeps: Map[String, String],
		additionalOptions: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit] = {
		val additionalOptionsStr = additionalOptions.mkString(" ")
		val baseCommand =
			s"pnpm add $additionalOptionsStr"
		val command = baseCommand + " " + NpmExecutor.depsString(deps.toSeq)
		val devCommand = baseCommand + " --save-dev " + NpmExecutor.depsString(devDeps.toSeq)

		cwd.foreach(sbt.IO.createDirectory)
		val existingResult = Process(baseCommand, cwd, environment.toSeq*).run().exitValue()
		val result = Process(command, cwd, environment.toSeq*).run().exitValue()
		val devResult = Process(devCommand, cwd, environment.toSeq*).run().exitValue()

		for {
			_ <- if (existingResult == 0) Right(()) else Left(s"Failed to install npm dependencies using npm. Exit code: $result")
			_ <- if (result == 0) Right(()) else Left(s"Failed to install npm dependencies using npm. Exit code: $result")
			_ <- if (devResult == 0) Right(()) else Left(s"Failed to install npm dev dependencies using npm. Exit code: $devResult")
		} yield ()
	}

	def run(
		pckg: String,
		module: String,
		arguments: Seq[String],
		environment: Map[String, String],
		cwd: Option[sbt.File],
	): Either[String, Unit] = {
		val baseCommand = s"pnpx exec $module"

		val command = s"$baseCommand ${arguments.mkString(" ")}"

		cwd.foreach(sbt.IO.createDirectory)
		val result = Process(command, cwd, environment.toSeq*).run().exitValue()

		if (result == 0) Right(())
		else Left(s"Failed to run $module in npm package $pckg using pnpx. Exit code: $result")
	}
}
