package app

import Analyser
import Parser
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("File name expected!")
        return
    }
    val text = File(args[0]).readText()
    val builder = StringBuilder()
    val prog = Parser(text).parseProgram().getOrThrow().also {it.print(builder)}
    println("Original file:")
    println(text)
    println("Parsed:")
    println(builder.toString())
    println("Unused assignments:")
    Analyser().findUnusedAssignments(prog).forEach{ println(it) }
}