package app

import Analyser
import Parser
import Program
import java.io.File

fun parseFile(fileName: String): Program {
    val text = File(fileName).readText()
    return Parser(text).parseProgram().getOrThrow()
}


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("File name expected!")
        return
    }

    try {
        val prog = parseFile(args[0])
        println("Parsed:")
        println(StringBuilder().also { prog.print(it) }.toString())
        println("Unused assignments:")
        Analyser().findUnusedAssignments(prog).forEach{ assign -> StringBuilder().also { assign.print(it); print(it) } }
    } catch(e: Exception) {
        println("Error: ${e.message}")
    }

}