package example.model

sealed trait Digit:
	self =>
		import Digit.*
		def char: Char = self match
			case D0 => '0'
			case D1 => '1'
			case D2 => '2'
			case D3 => '3'
			case D4 => '4'
			case D5 => '5'
			case D6 => '6'
			case D7 => '7'
			case D8 => '8'
			case D9 => '9'
			case Dot => '.'

object Digit:
	case object D0 extends Digit
	case object D1 extends Digit
	case object D2 extends Digit
	case object D3 extends Digit
	case object D4 extends Digit
	case object D5 extends Digit
	case object D6 extends Digit
	case object D7 extends Digit
	case object D8 extends Digit
	case object D9 extends Digit
	case object Dot extends Digit

	def fromChar(char: Char): Either[String, Digit] = char match
		case '0' => Right(D0)
		case '1' => Right(D1)
		case '2' => Right(D2)
		case '3' => Right(D3)
		case '4' => Right(D4)
		case '5' => Right(D5)
		case '6' => Right(D6)
		case '7' => Right(D7)
		case '8' => Right(D8)
		case '9' => Right(D9)
		case '.' => Right(Dot)
		case other => Left(s"'$other' is not a valid digit")
