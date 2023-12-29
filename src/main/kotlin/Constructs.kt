import kotlin.text.StringBuilder

data class Operator(val type: String)

fun Operator.print(builder: StringBuilder) {
    builder.append(" $type ")
}

sealed class Expression {
    fun print(builder: StringBuilder) {
        when(this) {
            is Operation -> {
                builder.append('(')
                left.print(builder).also{ operator.print(builder) }.also{ right.print(builder) }
                builder.append(")")
            }
            else -> builder.append(toString())
        }
    }
}
data class Variable(val name: String): Expression()
data class Constant(val value: String): Expression()
data class Operation(val left: Expression, val operator: Operator, val right: Expression): Expression()

sealed class Statement: StatementList() {
    override fun print(builder: StringBuilder, level: Int) {
        builder.append("\t".repeat(level))
        when (this) {
            is Assignment -> {
                left.print(builder).also { builder.append(" = ") }
                right.print(builder).also { builder.append("\n") }
            }
            is If -> {
                builder.append("if ")
                condition.print(builder).also { builder.append("\n") }
                body.print(builder, level + 1)
            }
            is While -> {
                builder.append("while ")
                condition.print(builder).also { builder.append("\n") }
                body.print(builder, level + 1)
            }
        }
    }

}
data class Assignment(val left: Variable, val right: Expression): Statement()
data class If(val condition: Expression, val body: StatementList): Statement()
data class While(val condition: Expression, val body: StatementList): Statement()

sealed class StatementList: Program()
data class NonEmptyStatementList(val head: Statement, val tail: StatementList): StatementList() {
    override fun print(builder: StringBuilder, level: Int) {
        head.print(builder, level)
        tail.print(builder, level)
    }
}

sealed class Program {
    abstract fun print(builder: StringBuilder, level: Int = 0)
}