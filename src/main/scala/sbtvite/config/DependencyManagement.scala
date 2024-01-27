package sbtvite.config

sealed trait DependencyManagement {
	self =>
	def isManual: Boolean = self match {
		case DependencyManagement.Manual => true
		case _ => false
	}

	def isFull: Boolean = self match {
		case DependencyManagement.Managed(_) => true
		case _ => false
	}

	def managerOption: Option[NpmManager] = self match {
		case DependencyManagement.Manual => None
		case DependencyManagement.InstallOnly(manager) => Some(manager)
		case DependencyManagement.Managed(manager) => Some(manager)
	}
}

object DependencyManagement {
	/**
	 * Runs vite relative to viteProjectRoot, and expects all required modules
	 * to be accessible there.
	 */
	case object Manual extends DependencyManagement

	/**
	 * Runs vite relative to viteProjectRoot, but installs npmDependencies
	 * and npmDevDependencies there.
	 *
	 * @param manager which npm dependency manager to use
	 */
	case class InstallOnly(manager: NpmManager = NpmManager.Npm)
	  extends DependencyManagement

	/**
	 * Runs vite in a managed directory, scoped to test or compile, installs
	 * npmDependencies and npmDevDependencies there, and copies over
	 * viteOtherSources for bundling.
	 *
	 * @param manager which npm dependency manager to use
	 */
	case class Managed(manager: NpmManager = NpmManager.Npm)
	  extends DependencyManagement
}
