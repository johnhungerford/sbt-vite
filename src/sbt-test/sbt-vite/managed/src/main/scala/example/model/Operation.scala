package example.model

enum Operation:
	self =>
		def stringValue: String = self match
			case Plus => "+"
			case Minus => "-"
			case Times => "x"
			case Divide => "÷"
		
	case Plus, Minus, Times, Divide