package example.util

import scala.scalajs.js.annotation.*

import example.model.Value

object FormatNumber:
	// Inefficient but strings won't be too big
	def addCommasNonDecimal(numberString: String): String =
		numberString
		  .reverse
		  .grouped(3)
		  .toList
		  .reverse
		  .map(_.reverse)
		  .mkString(",")

	/**
	 * Adds commas to a string-encoded number as appropriate. Detects decimals.
	 * @param numberString number encoded in a string
	 * @returns {string} A version of strin with commas added as appropriate
	 */
	@JSExportTopLevel("formatNumberString")
	def formatNumberString(numberString: String): String = numberString match {
		case Value.NonDecimal() => addCommasNonDecimal(numberString)
		case Value.Decimal(nonDecimal, decimal) =>
			val nonDecimalWithCommas = addCommasNonDecimal(nonDecimal)
			s"$nonDecimalWithCommas.$decimal"
		case "" => "0"
		case _ => throw new RuntimeException(s"Invalid number string: $numberString")
	}