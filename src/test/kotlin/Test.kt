import app.parseFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertEquals

class Test {
    private val prefix = "src/test/resources"


    @Test
    fun testExample() {
        assertDoesNotThrow {
            val prog = parseFile("$prefix/example.txt")
            val sol = parseFile("$prefix/example.sol")
            assertEquals(sol.toSet(), Analyser().findUnusedAssignments(prog))
        }
    }
}

fun Program.toSet(): Set<Statement> {
    return when(this) {
        is NonEmptyStatementList -> mutableSetOf(head).apply { addAll(tail.toSet()) }

        is Statement -> setOf(this)
    }
}