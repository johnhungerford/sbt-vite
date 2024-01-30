package example.model

import scala.util.Try
import scala.util.matching.Regex

final case class Value(stringValue: String, positive: Boolean):
	self =>
	def withDigit(digit: Digit): Value = digit match
		case Digit.Dot =>
			if stringValue.isEmpty then copy(stringValue = "0.")
			else if stringValue.endsWith(".") then self
			else self.copy(stringValue = stringValue.appended(Digit.Dot.char))
		case Digit.D0 =>
			if stringValue.isEmpty then self
			else self.copy(stringValue = stringValue.appended(digit.char))
		case digit => self.copy(stringValue = stringValue.appended(digit.char))

	lazy val togglePlusMinus: Value =
		copy(positive = !positive)

	lazy val numberValue: Either[String, Double] = Try(
		stringValue.trim match
			case Value.NonDecimal() => stringValue
			  .toDoubleOption
			  .toRight(s"Invalid number $stringValue")
			case Value.Decimal(left, right) =>
				if (right.trim.isEmpty)
					left.toDoubleOption.toRight(s"Invalid number $stringValue")
				else stringValue.toDoubleOption.toRight(s"Invalid number $stringValue")
			case "" => Right(0D)
	).toEither.left.map(_ => s"Invalid number $stringValue").flatten
	 .map(v => if !positive then 0 - v else v)


object Value:
	val NonDecimal: Regex = """[0-9]+""".r
	val Decimal: Regex = """([0-9]*)\.([0-9]*)""".r

	val empty: Value = Value("", true)

	def fromNumber(number: Double): Value =
		if number.isWhole && number < 0 then Value(Math.abs(number.toInt).toString, false)
		else if number.isWhole then Value(number.toInt.toString, true)
		else if number < 0 then Value(Math.abs(number).toString, false)
		else Value(number.toString, true)
