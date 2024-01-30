package example.component

import example.model.*
import japgolly.scalajs.react.*

import scalajs.js
import scalajs.js.annotation.*
import vdom.html_<^.*

@js.native
@JSImport("/buttonPanel.css", JSImport.Namespace)
object ButtonPanelCss extends js.Object

@js.native
@JSImport("/component/Button.jsx", JSImport.Default)
object ButtonRaw extends js.Object

@js.native
trait ButtonRawProps extends js.Object:
	var name: String
	var clickHandler: js.Function0[Unit]


object ButtonPanel:
	val buttonComponent = JsComponent[ButtonRawProps, Children.None, Null](ButtonRaw)

	def Button(name: String, callback: Callback): VdomElement =
		val props = (new js.Object()).asInstanceOf[ButtonRawProps]
		props.name = name
		props.clickHandler = () => callback.runNow()
		buttonComponent(props)

	val component =
		val _ = ButtonPanelCss
		ScalaComponent.builder[Action.Dispatcher]
			.stateless
			.render_P(render)
			.build

	def render(dispatcher: Action.Dispatcher): VdomElement =
		<.div(
			^.className := "component-button-panel",
			<.div(
				^.className := "button-row",
				Button("AC", dispatcher.dispatch(Action.ClickCA)),
				Button("CE", dispatcher.dispatch(Action.ClickCE)),
				Button("+/-", dispatcher.dispatch(Action.ClickPlusMinus)),
				Button("÷", dispatcher.dispatch(Action.ClickOperation(Operation.Divide))),
			),
			<.div(
				^.className := "button-row",
				Button("7", dispatcher.dispatch(Action.ClickDigit(Digit.D7))),
				Button("8", dispatcher.dispatch(Action.ClickDigit(Digit.D8))),
				Button("9", dispatcher.dispatch(Action.ClickDigit(Digit.D9))),
				Button("x", dispatcher.dispatch(Action.ClickOperation(Operation.Times))),
			),
			<.div(
				^.className := "button-row",
				Button("4", dispatcher.dispatch(Action.ClickDigit(Digit.D4))),
				Button("5", dispatcher.dispatch(Action.ClickDigit(Digit.D5))),
				Button("6", dispatcher.dispatch(Action.ClickDigit(Digit.D6))),
				Button("+", dispatcher.dispatch(Action.ClickOperation(Operation.Plus))),
			),
			<.div(
				^.className := "button-row",
				Button("1", dispatcher.dispatch(Action.ClickDigit(Digit.D1))),
				Button("2", dispatcher.dispatch(Action.ClickDigit(Digit.D2))),
				Button("3", dispatcher.dispatch(Action.ClickDigit(Digit.D3))),
				Button("-", dispatcher.dispatch(Action.ClickOperation(Operation.Minus))),
			),
			<.div(
				^.className := "button-row",
				Button("0", dispatcher.dispatch(Action.ClickDigit(Digit.D0))),
				Button(".", dispatcher.dispatch(Action.ClickDigit(Digit.Dot))),
				Button("spacer", Callback.empty),
				Button("=", dispatcher.dispatch(Action.ClickEquals)),
			),
		)
