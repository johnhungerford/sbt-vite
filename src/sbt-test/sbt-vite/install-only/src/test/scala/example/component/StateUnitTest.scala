package example.component

import utest.*
import example.model.*

object StateUnitTest extends TestSuite:
	val tests = Tests:
		test("update") {
			val finalState = for {
				s0 <- State.initial.update(Action.ClickDigit(Digit.D1))
				s1 <- s0.update(Action.ClickDigit(Digit.D9))
				s2 <- s1.update(Action.ClickDigit(Digit.Dot))
				s3 <- s2.update(Action.ClickDigit(Digit.D0))
				s4 <- s3.update(Action.ClickDigit(Digit.D5))
				s5 <- s4.update(Action.ClickOperation(Operation.Minus))
				s6 <- s5.update(Action.ClickDigit(Digit.Dot))
				s7 <- s6.update(Action.ClickDigit(Digit.D0))
				s8 <- s7.update(Action.ClickDigit(Digit.D5))
				s9 <- s8.update(Action.ClickEquals)
			} yield s9

			assert(finalState == Right(State.Initial(Value("19", true))))
		}

