package example.util

import example.model.Operation

object ExecuteOperation:
	def executeOperation(operation: Operation, left: Double, right: Double): Either[String, Double] =
		operation match
			case Operation.Plus => Right(left + right)
			case Operation.Minus => Right(left - right)
			case Operation.Times => Right(left * right)
			case Operation.Divide =>
				if (right == 0) Left("No division by zero!")
				else Right(left / right)
				

