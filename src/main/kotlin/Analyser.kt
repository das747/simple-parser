class Analyser {
    private val unusedAssignments = mutableSetOf<Assignment>()
    private val lastAssignments = mutableMapOf<Variable, Assignment>()
    private val loops = mutableListOf<MutableMap<Variable, Boolean>>()

    private fun getUsedVariables(expr: Expression): Set<Variable> {
        return when (expr) {
            is Variable -> setOf(expr)
            is Constant -> emptySet()
            is Operation -> getUsedVariables(expr.left) + getUsedVariables(expr.right)
        }
    }

    private fun removeUsages(expr: Expression) =
        getUsedVariables(expr).forEach {
            updateFirstInLoopUsage(it, true)
            lastAssignments.remove(it)
        }

    private fun updateFirstInLoopUsage(key: Variable, used: Boolean) {
        if (loops.isNotEmpty() && !loops.last().containsKey(key)){
            loops.last()[key] = used
        }
    }

    private fun analyzeProgram(program: Program) {
        with(program) {
            when (this) {
                is NonEmptyStatementList -> analyzeProgram(head).also { analyzeProgram(tail) }
                is Assignment -> {
                    removeUsages(right)
                    lastAssignments[left]?.let { unusedAssignments.add(it) }
                    updateFirstInLoopUsage(left, false)
                    lastAssignments[left] = this
                }
                is If -> {
                    removeUsages(condition)
                    analyzeProgram(body)
                }
                is While -> {
                    loops.add(mutableMapOf())
                    removeUsages(condition)
                    analyzeProgram(body)
                    loops.removeLast().forEach {
                        updateFirstInLoopUsage(it.key, it.value)
                        if (it.value) {
                            lastAssignments.remove(it.key)
                        }
                    }
                }
            }
        }
    }

    fun findUnusedAssignments(program: Program): Set<Assignment> {
        analyzeProgram(program)
        lastAssignments.forEach { unusedAssignments.add(it.value) }
        return unusedAssignments
    }

}