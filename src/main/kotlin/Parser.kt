typealias Parsed<T> = Result<Pair<T, Int>>

class Parser(private val tokens: List<String>) {

    private fun matchToken(token: String, pos: Int): Parsed<String> {
        if (pos >= tokens.size || !Regex(token).matches(tokens[pos])) {
            return Result.failure(Error(""))
        }

        return Result.success(tokens[pos] to pos + 1)
    }

    private fun <T> parseAtom(regex: String, constructor: (String) -> T, pos: Int): Parsed<T> =
        matchToken(regex, pos).map { (match, pos) -> constructor(match) to pos}

    private val operatorPriorities = listOf(
        "[*/]",
        "[+\\-]",
        "[><]"
    )
    private val maxPriority = 2
    private fun parseOperator(pos: Int, priority: Int): Parsed<Operator> {
        if (priority < 0 || priority > maxPriority) {
            return Result.failure(Error("priority ot of bounds"))
        }
        return parseAtom(operatorPriorities[priority], ::Operator, pos)
    }

    private fun parseVariable(pos: Int): Parsed<Variable> = parseAtom("[a-z]", ::Variable, pos)
    private fun parseConstant(pos: Int): Parsed<Constant> = parseAtom("[0-9]+", ::Constant, pos)

    private fun parseAtomicExpression(pos: Int): Parsed<Expression> =
        matchToken("\\(", pos).flatMap { (_, pos) ->
            parseExpression(pos).flatMap { (exp, pos) ->
                matchToken("\\)", pos).map { (_, pos) ->
                    exp to pos
                }
            }
        }.flatRecover { parseVariable(pos) }.flatRecover { parseConstant(pos) }

    private fun parseExpression(pos: Int, priority: Int): Parsed<Expression> {
        if (priority < 0) return parseAtomicExpression(pos)
        val left = parseExpression(pos, priority - 1)
        return left.flatMap { (left, pos) ->
            parseOperator(pos, priority).flatMap { (op, pos) ->
                parseExpression(pos, priority - 1).map { (right, pos) ->
                    Operation(left, op, right) to pos
                }
            }
        }.flatRecover { left }
    }
    private fun parseExpression(pos: Int): Parsed<Expression> = parseExpression(pos, maxPriority)

    private fun <T: Statement> parseControlStatement(name: String, constructor: (Expression, StatementList) -> T, pos: Int) =
        matchToken(name, pos).flatMap { (_, pos) ->
            parseExpression(pos).flatMap { (cond, pos) ->
                parseStatementList(pos).flatMap {(body, pos) ->
                    matchToken("end", pos).map {(_, pos) ->
                        constructor(cond, body) to pos
                    }
                }
            }
        }

    private fun parseStatement(pos: Int): Parsed<Statement> =
        parseVariable(pos).flatMap {(v, pos) ->
            matchToken("=", pos).flatMap { (_, pos) ->
                parseExpression(pos).map {(exp, pos) ->
                    Assignment(v, exp) to pos
                }
            }
        }.flatRecover {
            parseControlStatement("if", ::If, pos)
        }.flatRecover {
            parseControlStatement("while", ::While, pos)
        }

    private fun parseStatementList(pos: Int): Parsed<StatementList> {
        val statement = parseStatement(pos)
            return statement.flatMap { (st, pos) ->
                parseStatementList(pos).map { (tail, pos) ->
                    NonEmptyStatementList(st, tail) to pos
                }
            }.flatRecover { statement }
    }

    fun parseProgram(): Result<Program>  = parseStatementList(0).map { (res, _) -> res }

}

fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    if (this.isSuccess) {
        return transform(this.getOrNull()!!)
    }
    return Result.failure(this.exceptionOrNull()!!)
}

fun <T> Result<T>.flatRecover(transform: (Throwable) -> Result<T>): Result<T> {
    if (this.isFailure) {
        return transform(this.exceptionOrNull()!!)
    }
    return Result.success(this.getOrNull()!!)
}