import com.das747.*
import com.das747.Number
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.exp
import kotlin.test.assertEquals

class Test {
    private val matchers = listOf(
        Whitespace.Companion,
        Digit.Companion,
        LeftBracket.Companion,
        RightBracket.Companion,
        Operator.Companion,
        MinusOperator.Companion
    )

    private val lexer = Lexer(matchers)
    private val parser = Parser()

    private fun constant(sign: Boolean, vararg digits: String): Expression {
        return ConstantExpression(sign, Number(digits.map(::Digit)))
    }

    private fun pos(vararg digits: String): Expression =
        constant(true, *digits)

    private fun neg(vararg digits: String): Expression =
        constant(false, *digits)

    private fun expr(left: Expression, op: String, right: Expression): Expression {
        return BinaryExpression(left, Operator(op), right)
    }

    private fun parseAndCompare(expr: String, expected: Expression) {
        val parsed = parser.parseExpression(lexer.tokenize(expr))
        assertEquals(expected, parsed)
    }

    private inline fun <reified T : ParseError> parseAndCatch(expr: String) {
        assertThrows<T> { parser.parseExpression(lexer.tokenize(expr)) }
    }

    @Test
    fun numbers() {
        parseAndCompare("1", pos("1"))
        parseAndCompare("42", pos("4", "2"))
        parseAndCompare("-123", neg("1", "2", "3"))
    }

    @Test
    fun binaryExpressions() {
        parseAndCompare(
            "(1 + 1)",
            expr(pos("1"), "+", pos("1"))
        )
        parseAndCompare(
            "(-22 - 33)",
            expr(neg("2", "2"), "-", pos("3", "3"))
        )
        parseAndCompare(
            "(3 * -2)",
            expr(pos("3"), "*", neg("2"))
        )

        parseAndCompare(
            "((21 - 98) * (888 + (-1 * 2)))",
            expr(
                expr(pos("2", "1"), "-", pos("9", "8")),
                "*",
                expr(pos("8", "8", "8"), "+", expr(neg("1"), "*", pos("2")))
            )
        )

        parseAndCompare(
            "((21 -   98)   * (   888 + (-1        * 2))   )",
            expr(
                expr(pos("2", "1"), "-", pos("9", "8")),
                "*",
                expr(pos("8", "8", "8"), "+", expr(neg("1"), "*", pos("2")))
            )
        )
    }

    @Test
    fun invalidExpressions() {
        parseAndCatch<LexerError>("invalid expression")
        parseAndCatch<LexerError>("(1 / 2)")
        parseAndCatch<UnconsumedTokensError>("(1 + 2) * 3)")
        parseAndCatch<UnconsumedTokensError>("2 * 2")
        parseAndCatch<UnableToParseError>("+123")
        parseAndCatch<UnableToParseError>("*123")
        parseAndCatch<UnableToParseError>("((1 + 2) * 3")
    }
}