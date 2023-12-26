data class Operator(val type: String)

sealed class Expression
data class Variable(val name: String): Expression()
data class Constant(val value: String): Expression()
data class Operation(val left: Expression, val operator: Operator, val right: Expression): Expression()

sealed class Statement: StatementList()
data class Assignment(val left: Variable, val right: Expression): Statement()
data class If(val condition: Expression, val body: StatementList): Statement()
data class While(val condition: Expression, val body: StatementList): Statement()

sealed class StatementList: Program()
data class NonEmptyStatementList(val statement: Statement, val tail: StatementList): StatementList()

sealed class Program