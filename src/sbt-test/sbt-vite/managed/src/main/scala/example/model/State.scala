package example.model

import example.util.ExecuteOperation

sealed trait State:
	self =>
		def update(action: Action): Either[String, State] = action match
			case Action.ClickOperation(nextOperation) => self match
				case State.Initial(value) => for {
					initialNumber <- value.numberValue
				} yield State.Next(initialNumber, Some(nextOperation), Value.empty)
				case State.Next(currentTotal, None, nextValue) =>
					Right(State.Next(currentTotal, Some(nextOperation), nextValue))
				case State.Next(currentTotal, Some(operation), nextValue) => for {
					currentNumber <- nextValue.numberValue
					result <- ExecuteOperation.executeOperation(operation, currentTotal, currentNumber)
				} yield State.Next(
					result,
					Some(nextOperation),
					Value.empty,
				)
			case Action.ClickDigit(digit) => self match
				case State.Initial(nextValue) =>
					Right(State.Initial(nextValue.withDigit(digit)))
				case next: State.Next =>
					Right(next.copy(nextValue = next.nextValue.withDigit(digit)))
			case Action.ClickPlusMinus => self match
				case State.Initial(nextValue) =>
					Right(State.Initial(nextValue.togglePlusMinus))
				case next: State.Next =>
					Right(next.copy(nextValue = next.nextValue.togglePlusMinus))
			case Action.ClickEquals => self match
				case State.Initial(_) => Right(self)
				case State.Next(_, None, _) => Right(self)
				case State.Next(currentTotal, Some(operation), nextValue) => for {
					nextNumber <- nextValue.numberValue
					result <- ExecuteOperation.executeOperation(operation, currentTotal, nextNumber)
				} yield State.Initial(Value.fromNumber(result))
			case Action.ClickCA => Right(State.Initial(Value.empty))
			case Action.ClickCE => self match
				case State.Initial(_) =>
					Right(State.Initial(Value.empty))
				case State.Next(currentTotal, operation, _) =>
					Right(State.Next(currentTotal, operation, Value.empty))

object State:
	val initial: State = Initial(Value.empty)
	
	final case class Initial(nextValue: Value) extends State
	final case class Next(
		currentTotal: Double,
		operation: Option[Operation],
		nextValue: Value,
	) extends State