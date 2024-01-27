package sbtvite.config

import sbt._

import java.nio.file.Path

sealed trait Location {
	self =>
	def /(next: String): Location = self match {
		case Location.Root => Location.FromRoot(file(next))
		case Location.ProjectRoot => Location.FromProject(file(next))
		case Location.FromRoot(path) => Location.FromRoot(path / next)
		case Location.FromProject(path) => Location.FromProject(path / next)
		case Location.FromCwd(path) => Location.FromCwd(path / next)
	}

	def resolve(root: File, projectRoot: File): File = self match {
		case Location.Root => root
		case Location.ProjectRoot => projectRoot
		case Location.FromRoot(file) =>
			root.toPath.resolve(file.toPath).toFile
		case Location.FromProject(file) =>
			projectRoot.toPath.resolve(file.toPath).toFile
		case Location.FromCwd(file) => file
	}
}

object Location {
	def apply(path: String): Location.FromCwd = apply(file(path))

	def apply(file: File): Location.FromCwd = Location.FromCwd(file)

	case object Root extends Location

	case object ProjectRoot extends Location

	case class FromRoot(path: File) extends Location {
		require(!path.isAbsolute)
	}

	case class FromProject(path: File) extends Location {
		require(!path.isAbsolute)
	}

	case class FromCwd(path: File) extends Location
}