package com.das747

import java.util.LinkedList

interface Matcher {
    val regex: Regex
    fun tokenize(match: MatchResult): Token
}

sealed class Token

class Whitespace: Token() {
    companion object: Matcher {
        override val regex = Regex("\\s+")

        override fun tokenize(match: MatchResult): Token =
            Whitespace()
    }
}

data class Digit(val value: String): Token() {
    companion object: Matcher {
        override val regex = Regex("\\d")

        override fun tokenize(match: MatchResult): Token =
            Digit(match.value)
    }
}

open class Operator(val value: String): Token() {
    companion object: Matcher {
        override val regex = Regex("[+*]")

        override fun tokenize(match: MatchResult): Token =
            Operator(match.value)
    }

    override fun equals(other: Any?): Boolean {
        return other is Operator && value == other.value
    }
}

class MinusOperator: Operator("-") {
    companion object: Matcher {
        override val regex = Regex("-")

        override fun tokenize(match: MatchResult): Token =
            MinusOperator()
    }
}

class LeftBracket: Token() {
    companion object: Matcher {
        override val regex = Regex("\\(")

        override fun tokenize(match: MatchResult): Token =
            LeftBracket()
    }
}

class RightBracket: Token() {
    companion object: Matcher {
        override val regex = Regex("\\)")

        override fun tokenize(match: MatchResult): Token =
            RightBracket()
    }
}

class LexerError(pos: Int): ParseError("Error: Token match failure at pos $pos")

class Lexer(private val matchers: List<Matcher> ) {
    fun tokenize(string: String): List<Token> {
        val (tokens, index) = tokenize(string, 0)
        if (index < string.length) {
            throw LexerError(index)
        }
        return ArrayList(tokens)
    }

    private fun tokenize(string: String, index: Int): Pair<MutableList<Token>, Int> {
        match(string, index)?.let { (token, index) ->
            return tokenize(string, index).also { it.first.add(0, token) }
        }
        return LinkedList<Token>() to index
    }

    private fun match(string: String, index: Int): Pair<Token, Int>? {
        for (matcher in matchers) {
            matcher.regex.matchAt(string, index)?.let {
                return matcher.tokenize(it) to (index + it.value.length)
            }
        }
        return null
    }
}