package example.model

import japgolly.scalajs.react.Callback

enum Action:
	case ClickOperation(operation: Operation)
	case ClickDigit(digit: Digit)
	case ClickPlusMinus
	case ClickEquals
	case ClickCA
	case ClickCE
	
object Action:
	trait Dispatcher:
		def dispatch(action: Action): Callback