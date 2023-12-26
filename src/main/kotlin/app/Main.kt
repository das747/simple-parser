package app

import Parser

fun main(args: Array<String>) {
    Parser(listOf("a", "=", "b", "+", "c", "d", "=", "1")).parseProgram()
        .onSuccess { println(it) }
        .onFailure { println("Failure(") }
}