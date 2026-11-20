package com.gemwallet.android.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testPrecedence() {
        assertEquals("14", CalculatorEngine.evaluate("2 + 3 * 4"))
        assertEquals("20", CalculatorEngine.evaluate("(2 + 3) * 4"))
    }

    @Test
    fun testParentheses() {
        assertEquals("8", CalculatorEngine.evaluate("2 + (3 * 2)"))
        assertEquals("2", CalculatorEngine.evaluate("((1 + 1))"))
    }

    @Test
    fun testDecimals() {
        assertEquals("5.5", CalculatorEngine.evaluate("2.5 + 3"))
        assertEquals("0.3", CalculatorEngine.evaluate("0.1 + 0.2"))
    }

    @Test
    fun testPercent() {
        assertEquals("50", CalculatorEngine.evaluate("500 * 10%"))
        assertEquals("0.1", CalculatorEngine.evaluate("10%"))
    }

    @Test
    fun testNegativeNumbers() {
        assertEquals("-5", CalculatorEngine.evaluate("-2 - 3"))
        assertEquals("1", CalculatorEngine.evaluate("-2 + 3"))
        assertEquals("6", CalculatorEngine.evaluate("-2 * -3"))
    }

    @Test
    fun testDivisionByZero() {
        assertEquals("Error", CalculatorEngine.evaluate("5 / 0"))
    }

    @Test
    fun testLongInputs() {
        assertEquals("100", CalculatorEngine.evaluate("10 + 20 + 30 + 40"))
    }
}
