class Analyser {
    private val unusedAssignments = mutableSetOf<Assignment>()
    private val lastAssignments = mutableMapOf<String, Assignment>()

    private fun getUsedVariables(expr: Expression): Set<Variable> {
        return when (expr) {
            is Variable -> setOf(expr)
            is Constant -> emptySet()
            is Operation -> getUsedVariables(expr.left) + getUsedVariables(expr.right)
        }
    }

    private fun removeUsages(expr: Expression) =
        getUsedVariables(expr).forEach{lastAssignments.remove(it.name)}

    private fun analyzeProgram(program: Program) {
        with(program) {
            when (this) {
                is NonEmptyStatementList -> analyzeProgram(head).also { analyzeProgram(tail) }
                is Assignment -> {
                    removeUsages(right)
                    lastAssignments[left.name]?.let { unusedAssignments.add(it) }
                    lastAssignments[left.name] = this
                }
                is If -> {
                    removeUsages(condition)
                    analyzeProgram(body)
                }
                else -> TODO("loop are unimplemented")
            }
        }
    }

    fun findUnusedAssignments(program: Program): Set<Assignment> {
        analyzeProgram(program)
        lastAssignments.forEach { unusedAssignments.add(it.value) }
        return unusedAssignments
    }

}