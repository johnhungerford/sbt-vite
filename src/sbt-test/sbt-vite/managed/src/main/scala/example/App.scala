package simple

import example.component.{ButtonPanel, Display}
import example.model.Action.Dispatcher

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import example.model.{Action, State}

@js.native
@JSImport("react-toastify", "toast")
object toast extends js.Function1[String, Unit]:
	override def apply(arg1: String): Unit = js.native

@js.native
@JSImport("react-toastify", "ToastContainer")
object ToastRaw extends js.Object

@js.native
@JSImport("/app.css", JSImport.Namespace)
object AppCss extends js.Object

object App {
	private val Toast =
		JsComponent.apply[Null, Children.None, Null](ToastRaw)

	class Backend($: BackendScope[Unit, State]):
		def dispatcher: Dispatcher = new Dispatcher:
			override def dispatch(action: Action): Callback =
				$.modState { state =>
					state.update(action) match
						case Left(str) =>
							toast(str)
							state
						case Right(newState) =>
							newState
				}

		def render(state: State): VdomElement =
			val (displayValue, displayOperation) = state match
				case State.Initial(nextValue) => (nextValue, None)
				case State.Next(_, operationOpt, nextValue) => (nextValue, operationOpt)

			<.div(
				^.className := "component-webcalc-app",
				<.div(
					^.className := "webcalc-container",
					Display.component(Display.Props(displayValue, displayOperation)),
					ButtonPanel.component(dispatcher),
					Toast(),
				),
			)

	val component =
		val _ = AppCss

		ScalaComponent
			.builder[Unit]
			.initialState(State.initial)
			.backend[Backend](new Backend(_))
			.renderBackend
			.build

	@JSExportTopLevel("default")
	val rawApp =
		component
		  .cmapCtorProps[Unit](identity) // Change props from JS to Scala
		  .toJsComponent // Create a new, real JS component
		  .raw // Leave the nice Scala wrappers behind and obtain the underlying JS value

}
