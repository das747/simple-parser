package com.das747

sealed class Expression

data class ConstantExpression(val sign: Boolean, val number: Number) : Expression()

data class BinaryExpression(val left: Expression, val op: Operator, val right: Expression) : Expression()

data class Number(val digits: List<Digit>)

typealias Parsed<T> = Pair<T, Int>?

sealed class ParseError(msg: String) : RuntimeException(msg)

class UnconsumedTokensError: ParseError("\"Unconsumed tokens left\"")

class UnableToParseError: ParseError("Token sequence is not a valid expression")

class Parser {
    fun parseExpression(tokens: List<Token>, toEnd: Boolean = true): Expression {
        return tokens.filter { it !is Whitespace }.let { tokens ->
            parseExpression(tokens, 0)?.let { (res, pos) ->
                if (toEnd && pos < tokens.size) {
                    throw UnconsumedTokensError()
                }
                res
            } ?: throw UnableToParseError()
        }
    }

    private fun parseExpression(tokens: List<Token>, index: Int): Parsed<Expression> {
        return getToken<MinusOperator>(tokens, index)?.let { (_, index) ->
            parseNumber(tokens, index)?.let { (number, index) ->
                ConstantExpression(false, number) to index
            }
        } ?: parseNumber(tokens, index)?.let { (number, index) ->
            ConstantExpression(true, number) to index
        } ?: getToken<LeftBracket>(tokens, index)?.let { (_, index) ->
            parseExpression(tokens, index)?.let { (left, index) ->
                getToken<Operator>(tokens, index)?.let { (op, index) ->
                    parseExpression(tokens, index)?.let { (right, index) ->
                        getToken<RightBracket>(tokens, index)?.let { (_, index) ->
                            BinaryExpression(left, op, right) to index
                        }
                    }
                }
            }
        }
    }

    private fun parseNumber(tokens: List<Token>, index: Int): Parsed<Number> {
        return getOneOrMoreTokens<Digit>(tokens, index)?.let { (digits, index) ->
            Number(digits) to index
        }
    }

    private inline fun <reified T : Token> getToken(tokens: List<Token>, index: Int): Parsed<T> {
        return tokens.getOrNull(index).let {
            if (it is T) it to index + 1
            else null
        }
    }

    private inline fun <reified T : Token> getOneOrMoreTokens(
        tokens: List<Token>,
        index: Int
    ): Parsed<List<T>> {
        return tokens.takeFromWhile(index) { it is T }.filterIsInstance<T>().let {
            it to index + it.size
        }.takeUnless { it.first.isEmpty() }
    }
}

fun <T> List<T>.takeFromWhile(n: Int, predicate: (T) -> Boolean): List<T> {
    val res = mutableListOf<T>()
    for (i in n..<size) {
        if (predicate(get(i))) res.add(get(i))
        else break
    }
    return res
}