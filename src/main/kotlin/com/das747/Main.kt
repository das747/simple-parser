package com.das747

fun main() {
    val matchers = listOf(
        Whitespace.Companion,
        Digit.Companion,
        LeftBracket.Companion,
        RightBracket.Companion,
        Operator.Companion,
        MinusOperator.Companion
    )

    val lexer = Lexer(matchers)
    val parser = Parser()

    val tokens = lexer.tokenize("(3 + ((-1223 + 7) - 112))")
    val expr = parser.parseExpression(tokens)
}