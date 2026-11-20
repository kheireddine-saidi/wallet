package com.gemwallet.android.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object CalculatorEngine {
    private val mc = MathContext(16, RoundingMode.HALF_UP)

    fun evaluate(expression: String): String {
        if (expression.isBlank()) return "0"
        try {
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("–", "-")
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) return "0"
            val rpn = toRPN(tokens)
            val result = evalRPN(rpn)
            return formatResult(result)
        } catch (e: Exception) {
            return "Error"
        }
    }

    private sealed interface Token {
        data class Num(val value: BigDecimal) : Token
        data class Op(val symbol: String, val precedence: Int, val isRightAssoc: Boolean) : Token
        object LParen : Token
        object RParen : Token
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    var hasDot = (c == '.')
                    i++
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        if (expr[i] == '.') {
                            if (hasDot) throw IllegalArgumentException("Multiple dots")
                            hasDot = true
                        }
                        i++
                    }
                    val numStr = expr.substring(start, i)
                    tokens.add(Token.Num(BigDecimal(numStr)))
                    expectUnary = false
                }
                c == '+' || c == '-' -> {
                    if (expectUnary && c == '-') {
                        tokens.add(Token.Op("u-", 4, true))
                    } else {
                        tokens.add(Token.Op(c.toString(), 1, false))
                        expectUnary = true
                    }
                    i++
                }
                c == '*' || c == '/' -> {
                    tokens.add(Token.Op(c.toString(), 2, false))
                    expectUnary = true
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Op("%", 3, false))
                    expectUnary = false
                    i++
                }
                c == '(' -> {
                    tokens.add(Token.LParen)
                    expectUnary = true
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.RParen)
                    expectUnary = false
                    i++
                }
                else -> throw IllegalArgumentException("Unknown character: $c")
            }
        }
        return tokens
    }

    private fun toRPN(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val ops = mutableListOf<Token>()
        for (token in tokens) {
            when (token) {
                is Token.Num -> output.add(token)
                is Token.Op -> {
                    while (ops.isNotEmpty() && ops.last() is Token.Op) {
                        val top = ops.last() as Token.Op
                        if ((!token.isRightAssoc && token.precedence <= top.precedence) ||
                            (token.isRightAssoc && token.precedence < top.precedence)) {
                            output.add(ops.removeAt(ops.size - 1))
                        } else {
                            break
                        }
                    }
                    ops.add(token)
                }
                Token.LParen -> ops.add(token)
                Token.RParen -> {
                    while (ops.isNotEmpty() && ops.last() !is Token.LParen) {
                        output.add(ops.removeAt(ops.size - 1))
                    }
                    if (ops.isEmpty()) throw IllegalArgumentException("Mismatched parentheses")
                    ops.removeAt(ops.size - 1)
                }
            }
        }
        while (ops.isNotEmpty()) {
            val top = ops.removeAt(ops.size - 1)
            if (top is Token.LParen || top is Token.RParen) throw IllegalArgumentException("Mismatched parentheses")
            output.add(top)
        }
        return output
    }

    private fun evalRPN(rpn: List<Token>): BigDecimal {
        val stack = mutableListOf<BigDecimal>()
        for (token in rpn) {
            when (token) {
                is Token.Num -> stack.add(token.value)
                is Token.Op -> {
                    if (token.symbol == "u-") {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid expression")
                        val a = stack.removeAt(stack.size - 1)
                        stack.add(a.negate())
                    } else if (token.symbol == "%") {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid expression")
                        val a = stack.removeAt(stack.size - 1)
                        stack.add(a.divide(BigDecimal(100), mc))
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Invalid expression")
                        val b = stack.removeAt(stack.size - 1)
                        val a = stack.removeAt(stack.size - 1)
                        val res = when (token.symbol) {
                            "+" -> a.add(b, mc)
                            "-" -> a.subtract(b, mc)
                            "*" -> a.multiply(b, mc)
                            "/" -> {
                                if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                                a.divide(b, 10, RoundingMode.HALF_UP)
                            }
                            else -> throw IllegalArgumentException("Unknown operator")
                        }
                        stack.add(res)
                    }
                }
                else -> throw IllegalArgumentException("Unexpected token")
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
        return stack[0]
    }

    private fun formatResult(bd: BigDecimal): String {
        val stripped = bd.stripTrailingZeros()
        return stripped.toPlainString()
    }
}
