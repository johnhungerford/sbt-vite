package sbtvite.config

sealed trait NpmManager

object NpmManager {
	case object Yarn extends NpmManager

	case object Npm extends NpmManager

	case object Pnpm extends NpmManager
}