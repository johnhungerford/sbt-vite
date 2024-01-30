package example.component

import scalajs.js
import scalajs.js.annotation.*
import japgolly.scalajs.react.*
import example.model.{Operation, Value}

@js.native
@JSImport("/component/Display.jsx", JSImport.Default)
object DisplayRaw extends js.Object

@js.native
trait DisplayRawProps extends js.Object:
	var value: String
	var operation: String
	var positive: Boolean

object Display:
	private val RawComponent = JsComponent[DisplayRawProps, Children.None, Null](DisplayRaw)

	final case class Props(value: Value, operation: Option[Operation]):
		def raw: DisplayRawProps =
			val props = (js.Object()).asInstanceOf[DisplayRawProps]
			props.operation = operation.fold(" ")(_.stringValue)
			props.value = value.stringValue
			props.positive = value.positive
			props

	val component = ScalaComponent.builder[Props]
		.stateless
		.render_P { props =>
		  RawComponent(props.raw)
		}.build
