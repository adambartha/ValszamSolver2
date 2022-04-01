package core

import core.linear.LinearSolver
import core.ui.IUserInterface
import exceptions.*
import objects.*
import variables.*
import java.awt.Color
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.math.*

object SolverEngine
{
    private const val TITLE = "ValszámSolver 2.0"
    private const val STEP_LIMIT = 100
    private lateinit var ui: IUserInterface
    fun getTitle(): String = TITLE
    fun getStepLimit(): Int = STEP_LIMIT
    fun getUI(): IUserInterface = ui
    fun setUI(_ui: IUserInterface)
    {
        ui = _ui
    }
    fun print(message: String)
    {
        ui.messageOut(message)
    }
    private fun isInvalidOpChar(buffer: String, char: Char): Boolean
    {
        return !char.isLetterOrDigit() && buffer.last() == char
    }
    fun solve(commands: MutableList<String>): MutableList<Double>
    {
        val results = mutableListOf<Double>()
        if(commands.isEmpty())
        {
            ui.setExecTime("Üres bemenet")
            ui.setOutput(null, null)
            return results
        }
        var state = SolverState.INPUT
        var lineCount = 0
        var charCount = 0
        val timeBegin = System.nanoTime()
        try
        {
            ui.setOutput(null, Color.BLACK)
            val setSolver = SetSolver()
            val linearSolver = LinearSolver()
            for(line in commands)
            {
                lineCount++
                charCount = 0
                var buffer = ""
                var queryMode = false
                var commentMode = false
                for(char in line)
                {
                    if(commentMode)
                    {
                        break
                    }
                    charCount++
                    when(state)
                    {
                        SolverState.INPUT -> if(char.isLetterOrDigit())
                        {
                            buffer += char
                        }
                        else
                        {
                            when(char)
                            {
                                '(' -> when(buffer)
                                {
                                    "P" -> {
                                        state = if (queryMode) SolverState.QUERY_PROBABILITY else SolverState.PROBABILITY_DEFINE
                                        buffer = ""
                                    }
                                    "fgt", "kiz" -> {
                                        state = if (queryMode) SolverState.QUERY_MUTUAL else SolverState.PROBABILITY_MUTUAL
                                        buffer += ';'
                                    }
                                    "huz", "KI", "VH" -> {
                                        if(queryMode)
                                        {
                                            throw InvalidExpressionException(line)
                                        }
                                        when(buffer)
                                        {
                                            "huz" -> state = SolverState.DRAW_DEFINE
                                            "KI" -> state = SolverState.CINT_DEFINE
                                            "VH" -> state = SolverState.BAYESIAN_PARENTS
                                        }
                                        buffer = ""
                                    }
                                    else -> {
                                        if(!queryMode)
                                        {
                                            throw InvalidExpressionException(line)
                                        }
                                        state = when(buffer)
                                        {
                                            "E" -> SolverState.QUERY_EXPVALUE
                                            "V" -> SolverState.QUERY_VARIANCE
                                            "D" -> SolverState.QUERY_DEVIATION
                                            "emp" -> SolverState.QUERY_EMP
                                            "empk" -> SolverState.QUERY_EMPCORR
                                            "F" -> SolverState.QUERY_CDF
                                            "cov" -> SolverState.QUERY_COV
                                            "R", "corr" -> SolverState.QUERY_CORR
                                            "fi" -> SolverState.QUERY_PHI
                                            "ifi" -> SolverState.QUERY_INVERSEPHI
                                            "atlag" -> SolverState.QUERY_AVERAGE
                                            else -> throw InvalidExpressionException(line)
                                        }
                                        buffer = ""
                                    }
                                }
                                '{' -> {
                                    if(buffer.isNotEmpty())
                                    {
                                        throw InvalidExpressionException(line)
                                    }
                                    state = SolverState.SEQ_DEFINE
                                }
                                '~', '=' -> {
                                    if(buffer.isEmpty())
                                    {
                                        throw InvalidExpressionException(line)
                                    }
                                    Repository.checkVariable(buffer)
                                    buffer += ';'
                                    state = if (char == '~') SolverState.PVAR_DEFINE else SolverState.OBJECT_DEFINE
                                }
                                ',' -> {
                                    if(buffer.isEmpty())
                                    {
                                        throw InvalidExpressionException(line)
                                    }
                                    buffer += char
                                    state = SolverState.JVAR_DEFINE
                                }
                                '#' -> commentMode = true
                                '?' -> {
                                    if(buffer.isNotEmpty())
                                    {
                                        throw InvalidExpressionException(line)
                                    }
                                    queryMode = true
                                }
                                else -> throw InvalidCharacterException(char)
                            }
                        }
                        SolverState.PROBABILITY_DEFINE -> if(char == '=')
                        {
                            if(buffer.length < 2 || !buffer.endsWith(")"))
                            {
                                throw InvalidOperationException()
                            }
                            buffer = "${buffer.dropLast(1)}="
                        }
                        else if(char in ".+*/|()" || char.isLetterOrDigit())
                        {
                            buffer += char
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.PROBABILITY_MUTUAL -> if(char.isLetterOrDigit())
                        {
                            buffer += char
                        }
                        else if(char == ',')
                        {
                            if(buffer.indexOf(';') != buffer.lastIndexOf(';'))
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += ';'
                        }
                        else if(char == ')')
                        {
                            val data = buffer.split(';')
                            if(data[0] == "fgt")
                            {
                                setSolver.makeIndependent(data[1], data[2])
                            }
                            else
                            {
                                setSolver.makeExclusive(data[1], data[2])
                            }
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        SolverState.DRAW_DEFINE -> if(char == ',' || char.isLetterOrDigit())
                        {
                            if(char == ',' && char in buffer)
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == ')')
                        {
                            val data = buffer.split(',')
                            if(data.size == 1)
                            {
                                throw InvalidExpressionException(line)
                            }
                            val box = Repository.getBox(data[0]) ?: throw UnknownVariableException(data[0])
                            if(data[1] !in "vk")
                            {
                                throw InvalidParameterException(data[1])
                            }
                            box.draw(data[1] == "v")
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.CINT_DEFINE -> if(char in ".,/" || char.isLetterOrDigit())
                        {
                            if(isInvalidOpChar(buffer, char))
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == ')')
                        {
                            val data = buffer.split(',')
                            val n: Int; val mean: Double; val deviation: Double; val level: Double
                            when(data.size)
                            {
                                2 -> {
                                    val sample = Repository.getSample(data[0]) ?: throw UnknownVariableException(data[0])
                                    n = sample.getN()
                                    mean = sample.getMean()
                                    deviation = sample.getDeviation()
                                    level = Utility.getValue(data[1]) * 1e-2
                                }
                                4 -> {
                                    n = data[0].toInt()
                                    mean = Utility.getValue(data[1])
                                    deviation = Utility.getValue(data[2])
                                    level = Utility.getValue(data[3]) * 1e-2
                                }
                                else -> throw InvalidExpressionException(line)
                            }
                            val cInt = CInt(n, mean, deviation, level)
                            val prec = ui.getPrecision()
                            print("α = %.${prec}f, u = Φ*(%.${prec}f) = %.${prec}f".format(cInt.getSig(), cInt.getInverseU(), cInt.getU()))
                            print("→ (%.${prec}f, %.${prec}f)".format(cInt.getMin(), cInt.getMax()))
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.PVAR_DEFINE -> if(char == '(')
                        {
                            buffer += ';'
                            state = SolverState.PVAR_PARAMS
                        }
                        else if(char == ':')
                        {
                            val type = buffer.substringAfter(';')
                            if(type !in arrayOf("Geo", "Pois", "Exp"))
                            {
                                throw ResolveException(type)
                            }
                            buffer += ';'
                            state = SolverState.PVAR_RESOLVE
                        }
                        else if(char.isLetter())
                        {
                            buffer += char
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.PVAR_PARAMS -> if(char == ')')
                        {
                            val data = buffer.split(';')
                            when(data[1])
                            {
                                "Geo" -> {
                                    val p = Utility.getValue(data[2])
                                    if(p < 0 || p > 1)
                                    {
                                        throw InvalidParameterException("'p' értékének 0 és 1 között kell lennie")
                                    }
                                    Repository.addVar(data[0], VarGeometric(p))
                                }
                                "B" -> {
                                    val n = data[2].toInt(); val p = Utility.getValue(data[3])
                                    if(n < 0)
                                    {
                                        throw InvalidParameterException("'n' értéke nem lehet negatív")
                                    }
                                    if(p < 0 || p > 1)
                                    {
                                        throw InvalidParameterException("'p' értékének 0 és 1 között kell lennie")
                                    }
                                    Repository.addVar(data[0], VarBinomial(n, p))
                                }
                                "Pois" -> {
                                    val l = Utility.getValue(data[2])
                                    if(l <= 0)
                                    {
                                        throw InvalidParameterException("'λ' értéke nem lehet 0 vagy negatív")
                                    }
                                    Repository.addVar(data[0], VarPoisson(l))
                                }
                                "U" -> {
                                    val a = Utility.getValue(data[2]); val b = Utility.getValue(data[3])
                                    Repository.addVar(data[0], VarUniform(a.coerceAtMost(b), a.coerceAtLeast(b)))
                                }
                                "Exp" -> {
                                    val l = Utility.getValue(data[2])
                                    if(l <= 0)
                                    {
                                        throw InvalidParameterException("'λ' értéke nem lehet 0 vagy negatív")
                                    }
                                    Repository.addVar(data[0], VarExponential(l))
                                }
                                "N" -> {
                                    val mean = Utility.getValue(data[2])
                                    val variance = Utility.getValue(data[3])
                                    if(variance <= 0)
                                    {
                                        throw InvalidParameterException("'σ²' értéke nem lehet 0 vagy negatív")
                                    }
                                    Repository.addVar(data[0], VarNormal(mean, variance))
                                }
                                else -> throw InvalidPVarException(data[1])
                            }
                            state = SolverState.INPUT
                        }
                        else if(char == ',')
                        {
                            buffer += ';'
                        }
                        else if(char in "/-." || char.isDigit())
                        {
                            buffer += char
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.PVAR_RESOLVE -> if(char in "EVD")
                        {
                            if(char in buffer.substringAfterLast(';'))
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == '=')
                        {
                            val params = buffer.substringAfterLast(';')
                            if(char in params || "EVD".all { it !in params })
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += ';'
                        }
                        else if(char in "-./" || char.isDigit())
                        {
                            buffer += char
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.JVAR_DEFINE -> if(char in ".,/-" || char.isLetterOrDigit())
                        {
                            if(isInvalidOpChar(buffer, char) || char == ',' && '=' !in buffer && ',' in buffer)
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == '=')
                        {
                            if('=' in buffer)
                            {
                                throw InvalidExpressionException(line)
                            }
                            val vars = buffer.split(',')
                            Repository.checkVariable(vars[0])
                            Repository.checkVariable(vars[1])
                            buffer += char
                        }
                        else if(char == '{')
                        {
                            val data = buffer.split(',', '=').toTypedArray()
                            Repository.addJointVar(Utility.getJointKey(data[0], data[1], ','), data)
                            buffer = ""
                            state = SolverState.JVAR_PARAMS
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.JVAR_PARAMS -> if(char in ".,/-" || char.isLetterOrDigit())
                        {
                            if(buffer.isNotEmpty() && isInvalidOpChar(buffer, char))
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == '}')
                        {
                            Repository.closeJointVar()
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.OBJECT_DEFINE -> if(char == '(')
                        {
                            val data = buffer.split(';')
                            state = when(data[1])
                            {
                                "minta" -> SolverState.OBJECT_SAMPLE
                                "doboz" -> SolverState.OBJECT_BOX
                                "T" -> SolverState.OBJECT_REGION
                                else -> throw InvalidExpressionException(data[1])
                            }
                            buffer = data[0] + ';'
                        }
                        else if(char.isLetter())
                        {
                            buffer += char
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.OBJECT_SAMPLE -> if(char in ".,/" || char.isLetterOrDigit())
                        {
                            if(isInvalidOpChar(buffer, char))
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == ')')
                        {
                            val data = buffer.split(';')
                            Repository.addSample(data[0], Sample(data[1]))
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.OBJECT_BOX -> if(char == ',' || char.isLetterOrDigit())
                        {
                            if(char == ',' && buffer.last() == ',')
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == ')')
                        {
                            val data = buffer.split(';')
                            val dataValues = data[1].split(',')
                            val good = Integer.valueOf(dataValues[0])
                            val bad = Integer.valueOf(dataValues[1])
                            if(good == 0)
                            {
                                throw InvalidParameterException("'jó' darabszám nem lehet 0")
                            }
                            if(bad == 0)
                            {
                                throw InvalidParameterException("'rossz' darabszám nem lehet 0")
                            }
                            Repository.addBox(data[0], Box(good, bad))
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.OBJECT_REGION -> if(char == ',' || char.isLetterOrDigit())
                        {
                            if(char == ',' && buffer.last() == ',')
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == ')')
                        {
                            val data = buffer.split(';')
                            val dataValues = data[1].split(',')
                            val params = DoubleArray(dataValues.size)
                            for(i in params.indices)
                            {
                                params[i] = Utility.getValue(dataValues[i])
                            }
                            val region = when(params.size)
                            {
                                3 -> Circle(params[0], params[1], params[2])
                                4 -> Rect(params[0], params[1], params[2], params[3])
                                6 -> Triangle(params[0], params[1], params[2], params[3], params[4], params[5])
                                else -> throw InvalidExpressionException(line)
                            }
                            Repository.addRegion(data[0], region)
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.SEQ_DEFINE -> if(char in ".,/-" || char.isLetterOrDigit())
                        {
                            if(isInvalidOpChar(buffer, char))
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        else if(char == '}')
                        {
                            val data = buffer.split(',')
                            when(data[0])
                            {
                                "B" -> {
                                    val x = Utility.getValue(data[1])
                                    val y = Utility.getValue(data[2])
                                    val d = data[3].toInt()
                                    val p = (y - x) / d.toDouble()
                                    if(p > 1)
                                    {
                                        throw NoSolutionException("'p' értéke 1-nél nagyobb")
                                    }
                                    if(p < 0)
                                    {
                                        throw NoSolutionException("'p' értéke negatív")
                                    }
                                    val n = x / p
                                    if(!Utility.isWhole(n))
                                    {
                                        throw NoSolutionException("'n' értéke nem egész")
                                    }
                                    print("X~B(n,p): E(X)=${data[1]}, Y~B(n${if (d < 0) d else "+$d"},p): E(Y)=${data[2]}")
                                    print("→ p = ${Utility.format(p)}, n = ${n.roundToInt()}")
                                }
                                "Pois" -> {
                                    val x = data[1].toInt()
                                    val y = data[2].toInt()
                                    val l = exp(ln(Utility.factorial(x).toDouble() / Utility.factorial(y).toDouble()) / (x - y))
                                    print("X~Pois(λ): P(X=$x) = P(X=$y)")
                                    print("→ λ = e^[ln($x! / $y!) / ($x - $y)] = ${Utility.format(l)}")
                                }
                                "N" -> {
                                    val pX = Utility.getValue(data[1])
                                    val x = Utility.getValue(data[2])
                                    val pY = Utility.getValue(data[3])
                                    val y = Utility.getValue(data[4])
                                    val phiX = Utility.inversePhiFunction(pX)
                                    val d = (y - x) / (Utility.inversePhiFunction(1 - pY) - phiX)
                                    val m = x - phiX * d
                                    print("X~N(μ, σ²): P(X<${Utility.format(x)}) = ${Utility.format(pX)}, P(X>${Utility.format(y)}) = ${Utility.format(pY)}")
                                    print("→ μ = ${Utility.format(m)}, σ = ${Utility.format(d)}, σ² = ${Utility.format(d * d)}")
                                }
                                else -> throw NoSEQException()
                            }
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.BAYESIAN_PARENTS -> if(char.isLetterOrDigit())
                        {
                            buffer += char
                        }
                        else if(char in ",-")
                        {
                            if(buffer.isEmpty() || buffer.last() == char)
                            {
                                throw InvalidBayesianException(line)
                            }
                            buffer += char
                        }
                        else if(char == '>')
                        {
                            if(buffer.last() != '-')
                            {
                                throw InvalidBayesianException(line)
                            }
                            for(event in buffer.dropLast(1).split(','))
                            {
                                Repository.checkVariable(event)
                            }
                            buffer += char
                            state = SolverState.BAYESIAN_CHILDREN
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.BAYESIAN_CHILDREN -> if(char == ',')
                        {
                            if(',' in buffer.substring(0, buffer.indexOf('-')))
                            {
                                throw InvalidBayesianException(line)
                            }
                            buffer += char
                        }
                        else if(char == ')')
                        {
                            for(event in buffer.substring(buffer.indexOf('>') + 1).split(','))
                            {
                                Repository.checkVariable(event)
                            }
                            setSolver.addBayesianLink(buffer)
                            buffer = ""
                            state = SolverState.INPUT
                        }
                        else if(char.isLetterOrDigit())
                        {
                            buffer += char
                        }
                        else
                        {
                            throw InvalidCharacterException(char)
                        }
                        SolverState.QUERY_PROBABILITY -> {
                            if(char == '=')
                            {
                                if(buffer.isEmpty())
                                {
                                    throw InvalidExpressionException(line)
                                }
                                if(buffer.last() !in "<>" && !buffer.last().isLetterOrDigit())
                                {
                                    throw InvalidCharacterException(char)
                                }
                            }
                            else if(char == '>')
                            {
                                if(isInvalidOpChar(buffer, char) || char in buffer || '<' in buffer)
                                {
                                    throw InvalidExpressionException(line)
                                }
                            }
                            else if(char == '<')
                            {
                                val index = buffer.indexOf(char); val indexLast = buffer.lastIndexOf(char)
                                if('>' in buffer || indexLast - index > 0 && char in buffer.substring(index + 1, indexLast + 1))
                                {
                                    throw InvalidExpressionException(line)
                                }
                            }
                            else if(char !in "+*/|,()" && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_EXPVALUE -> {
                            if(char !in ".+-*/,^()" && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_VARIANCE -> {
                            if(char != ')' && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_DEVIATION -> {
                            if(char != ')' && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_EMP -> {
                            if(char != ')' && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_EMPCORR -> {
                            if(char != ')' && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_CDF -> {
                            if(';' in buffer)
                            {
                                val value = buffer.split(';')[1]
                                val invalidDiv = char == '/' && (value.isEmpty() || '/' in value || '.' in value)
                                val invalidDot = char == '.' && value.isNotEmpty() && '.' in value || '/' in value
                                if(char == ',')
                                {
                                    throw InvalidCharacterException(char)
                                }
                                else if((invalidDiv || invalidDot) && !char.isDigit())
                                {
                                    throw InvalidParameterException(line)
                                }
                                else if(char == ')' && value.isEmpty())
                                {
                                    throw InvalidExpressionException(line)
                                }
                                buffer += char
                            }
                            else
                            {
                                if(char == ',')
                                {
                                    if(buffer.isEmpty())
                                    {
                                        throw InvalidExpressionException(line)
                                    }
                                    if(!Repository.hasVar(buffer) && !Repository.hasSample(buffer))
                                    {
                                        throw UnknownVariableException(buffer)
                                    }
                                    buffer += ';'
                                }
                                else if(char == ')' || char.isLetterOrDigit())
                                {
                                    buffer += char
                                }
                                else
                                {
                                    throw InvalidCharacterException(char)
                                }
                            }
                        }
                        SolverState.QUERY_COV, SolverState.QUERY_CORR -> {
                            if(char == ',')
                            {
                                if(';' in buffer)
                                {
                                    throw InvalidExpressionException(line)
                                }
                                buffer += ';'
                            }
                            else if(char in "+-*/.()" || char.isLetterOrDigit())
                            {
                                buffer += char
                            }
                            else
                            {
                                throw InvalidExpressionException(line)
                            }
                        }
                        SolverState.QUERY_PHI, SolverState.QUERY_INVERSEPHI -> {
                            if(char == '-')
                            {
                                if(state != SolverState.QUERY_PHI)
                                {
                                    throw InvalidCharacterException(char)
                                }
                                if(buffer.isNotEmpty() && '-' in buffer)
                                {
                                    throw InvalidParameterException(buffer)
                                }
                            }
                            else if(char == '/')
                            {
                                if(buffer.isEmpty() || '/' in buffer || '.' in buffer)
                                {
                                    throw InvalidParameterException(buffer)
                                }
                            }
                            else if(char == '.')
                            {
                                if(buffer.isNotEmpty() && ('.' in buffer || '/' in buffer))
                                {
                                    throw InvalidParameterException(buffer)
                                }
                            }
                            else if(char != ')' && !char.isDigit())
                            {
                                throw InvalidExpressionException(line)
                            }
                            buffer += char
                        }
                        SolverState.QUERY_AVERAGE -> {
                            if(char != ')' && !char.isLetterOrDigit())
                            {
                                throw InvalidCharacterException(char)
                            }
                            buffer += char
                        }
                        else -> throw Exception("not implemented")
                    }
                }
                if(queryMode)
                {
                    var result: Double
                    var expression = line.drop(1)
                    if(buffer.isEmpty())
                    {
                        continue
                    }
                    if(buffer.last() != ')')
                    {
                        throw InvalidExpressionException(expression)
                    }
                    buffer = buffer.dropLast(1)
                    when(state)
                    {
                        SolverState.QUERY_PROBABILITY -> {
                            val isLessThan = '<' in buffer
                            val isGreaterThan = '>' in buffer
                            val isEqualTo = '=' in buffer
                            if(buffer.indexOf('<') != buffer.lastIndexOf('<'))
                            {
                                val data = buffer.split('<')
                                val key = data[1]
                                Repository.validateName(key)
                                val variable = Repository.getVar(key) ?: throw UnknownVariableException(key)
                                if(variable !is CVar)
                                {
                                    throw NonContinuousException(key)
                                }
                                val xMin = Utility.getValue(data[0])
                                val xMax = Utility.getValue(data[2])
                                result = variable.getCDF(xMax) - variable.getCDF(xMin)
                                expression += if(variable is VarNormal)
                                {
                                    "= Φ(${Utility.format((xMax - variable.getMean()) / variable.getDeviation())}) - Φ(${Utility.format((xMin - variable.getMean()) / variable.getDeviation())})"
                                }
                                else
                                {
                                    " = F($key,${data[2]}) - F($key,${data[0]})"
                                }
                            }
                            else if(isEqualTo)
                            {
                                if(isLessThan)
                                {
                                    val data = buffer.split("<=")
                                    val key = data[0]
                                    Repository.validateName(key)
                                    val variable = Repository.getVar(key) ?: throw UnknownVariableException(key)
                                    result = if(variable is DVar)
                                    {
                                        variable.getAtMost(data[1].toInt())
                                    }
                                    else
                                    {
                                        (variable as CVar).getCDF(Utility.getValue(data[1]))
                                    }
                                    expression = expression.replace("<=", "≤")
                                    if(variable is VarNormal)
                                    {
                                        expression += " = Φ(${Utility.format((Utility.getValue(data[1]) - variable.getMean()) / variable.getDeviation())})"
                                    }
                                }
                                else if(isGreaterThan)
                                {
                                    val data = buffer.split(">=")
                                    val key = data[0]
                                    Repository.validateName(key)
                                    val variable = Repository.getVar(key) ?: throw UnknownVariableException(key)
                                    result = if(variable is DVar)
                                    {
                                        variable.getAtLeast(data[1].toInt())
                                    }
                                    else
                                    {
                                        1.0 - (variable as CVar).getCDF(Utility.getValue(data[1]))
                                    }
                                    expression = "${expression.replace(">=", "≥")} = 1 - ${if(variable is VarNormal)
                                    {
                                        "Φ(${Utility.format((Utility.getValue(data[1]) - variable.getMean()) / variable.getDeviation())})"
                                    }
                                    else
                                    {
                                        "P($key<${data[1]})"
                                    }}"
                                }
                                else
                                {
                                    val data = buffer.split('=')
                                    val key = data[0]
                                    Repository.validateName(key)
                                    val variable = Repository.getVar(key)
                                    val jointVariable = Repository.getJointVar(key)
                                    result = jointVariable?.getMarginal(key, Utility.getValue(data[1])) ?: when(variable)
                                    {
                                        null -> throw UnknownVariableException(key)
                                        is DVar -> variable.getExact(data[1].toInt())
                                        else -> (variable as CVar).getPDF(Utility.getValue(data[1]))
                                    }
                                }
                            }
                            else
                            {
                                if(isLessThan)
                                {
                                    val data = buffer.split('<')
                                    val key = data[0]
                                    Repository.validateName(key)
                                    val variable = Repository.getVar(key) ?: throw UnknownVariableException(key)
                                    result = if(variable is DVar)
                                    {
                                        variable.getLessThan(data[1].toInt())
                                    }
                                    else
                                    {
                                        (variable as CVar).getCDF(Utility.getValue(data[1]))
                                    }
                                    if(variable is VarNormal)
                                    {
                                        expression += " = Φ(${Utility.format((Utility.getValue(data[1]) - variable.getMean()) / variable.getDeviation())})"
                                    }
                                }
                                else if(isGreaterThan)
                                {
                                    val data = buffer.split('>')
                                    val key = data[0]
                                    Repository.validateName(key)
                                    val variable = Repository.getVar(key) ?: throw UnknownVariableException(key)
                                    result = if(variable is DVar)
                                    {
                                        variable.getGreaterThan(data[1].toInt())
                                    }
                                    else
                                    {
                                        1.0 - (variable as CVar).getCDF(Utility.getValue(data[1]))
                                    }
                                    expression += " = 1 - ${if(variable is VarNormal)
                                    {
                                        "Φ(${(Utility.getValue(data[1]) - variable.getMean()) / variable.getDeviation()})"
                                    }
                                    else
                                    {
                                        "P($key≤${data[1]})"
                                    }}"
                                }
                                else
                                {
                                    result = setSolver.solve(buffer)
                                }
                            }
                        }
                        SolverState.QUERY_EXPVALUE -> {
                            result = linearSolver.solve(buffer)
                        }
                        SolverState.QUERY_VARIANCE -> {
                            result = if(Repository.hasVar(buffer))
                            {
                                Repository.getVar(buffer)!!.getVariance()
                            }
                            else if(Repository.hasSample(buffer))
                            {
                                Repository.getSample(buffer)!!.getEmpCorr()
                            }
                            else
                            {
                                val jointProb = Repository.getJointProbFromKey(buffer) ?: throw UnknownVariableException(buffer)
                                jointProb.getVariance(buffer)
                            }
                        }
                        SolverState.QUERY_DEVIATION -> {
                            result = if(Repository.hasVar(buffer))
                            {
                                Repository.getVar(buffer)!!.getDeviation()
                            }
                            else if(Repository.hasSample(buffer))
                            {
                                sqrt(Repository.getSample(buffer)!!.getEmpCorr())
                            }
                            else
                            {
                                val jointProb = Repository.getJointProbFromKey(buffer) ?: throw UnknownVariableException(buffer)
                                sqrt(jointProb.getVariance(buffer))
                            }
                        }
                        SolverState.QUERY_EMP, SolverState.QUERY_EMPCORR -> {
                            val sample = Repository.getSample(buffer) ?: throw UnknownVariableException(buffer)
                            result = if(state == SolverState.QUERY_EMP) sample.getEmp() else sample.getEmpCorr()
                        }
                        SolverState.QUERY_CDF -> {
                            val data = buffer.split(';')
                            val key = data[0]
                            val value = Utility.getValue(data[1])
                            if(Repository.hasVar(key))
                            {
                                val variable = Repository.getVar(key)!!
                                result = if(variable is DVar)
                                {
                                    variable.getLessThan(value.toInt())
                                }
                                else
                                {
                                    (variable as CVar).getCDF(value)
                                }
                            }
                            else if(Repository.hasSample(key))
                            {
                                result = Repository.getSample(key)!!.getCDF(value)
                            }
                            else
                            {
                                throw UnknownVariableException(key)
                            }
                        }
                        SolverState.QUERY_COV -> {
                            val data = buffer.split(';')
                            val key0 = data[0]; val key1 = data[1]
                            val jointKey = Utility.getJointKey(key0, key1, ',')
                            val jointProb = Repository.getJointVar(jointKey)
                            if(jointProb == null)
                            {
                                val inputXY = "($key0)*($key1)"
                                val eXY = linearSolver.solve(inputXY)
                                val eX = linearSolver.solve(key0)
                                val eY = linearSolver.solve(key1)
                                result = eXY - eX * eY
                                expression += "= E($inputXY) - E($key0)E($key1)"
                            }
                            else
                            {
                                result = jointProb.getCov()
                                expression += "= E($key0*$key1) - E($key0)E($key1)"
                            }
                        }
                        SolverState.QUERY_CORR -> {
                            val data = buffer.split(';')
                            val key0 = data[0]; val key1 = data[1]
                            val jointKey = Utility.getJointKey(key0, key1, ',')
                            val jointProb = Repository.getJointVar(jointKey)
                            if(jointProb == null)
                            {
                                if(Repository.getJointProbFromKey(key0) == null)
                                {
                                    throw UnknownVariableException(key0)
                                }
                                if(Repository.getJointProbFromKey(key1) == null)
                                {
                                    throw UnknownVariableException(key1)
                                }
                                throw UnknownVariableException(key0, key1)
                            }
                            result = jointProb.getCorr()
                        }
                        SolverState.QUERY_PHI, SolverState.QUERY_INVERSEPHI -> {
                            val value = Utility.getValue(buffer)
                            if(state == SolverState.QUERY_INVERSEPHI)
                            {
                                if(value < 0.0 || value > 1.0)
                                {
                                    throw InvalidParameterException("Φ*(x)-ben x-nek 0 és 1 között kell lennie")
                                }
                                result = Utility.inversePhiFunction(value)
                                expression = "Φ*($buffer)"
                            }
                            else
                            {
                                result = Utility.phiFunction(value, 0.0, 1.0)
                                expression = "Φ($buffer)%${if (value < 0.0) " = 1 - Φ(${buffer.drop(1)})" else ""}"
                            }
                        }
                        SolverState.QUERY_AVERAGE -> {
                            val sample = Repository.getSample(buffer) ?: throw UnknownVariableException(buffer)
                            result = sample.getMean()
                        }
                        else -> throw Exception("Undefined state")
                    }
                    print("→ ${if (state === SolverState.QUERY_PROBABILITY) Utility.toSetNotation(expression) else expression} = ${Utility.format(result)}")
                    results.add(result)
                    buffer = ""
                    state = SolverState.INPUT
                }
                if(!commentMode)
                {
                    if(state === SolverState.PROBABILITY_DEFINE)
                    {
                        val data = buffer.split('=')
                        setSolver.add(data[0], Utility.getValue(data[1]))
                        state = SolverState.INPUT
                    }
                    else if(state === SolverState.JVAR_PARAMS)
                    {
                        if(buffer.isNotEmpty())
                        {
                            Repository.appendJointVar(buffer)
                        }
                        else if(!Repository.isJointMode())
                        {
                            state = SolverState.INPUT
                        }
                    }
                    else if(state === SolverState.PVAR_RESOLVE)
                    {
                        val data = buffer.split(';')
                        val key = data[0]
                        Repository.checkVariable(key)
                        val value = Utility.getValue(data[3])
                        when(data[1])
                        {
                            "Geo" -> {
                                val p = when(data[2])
                                {
                                    "E" -> 1.0 / value
                                    "V" -> (sqrt(4.0 * value + 1.0) - 1.0) / (2.0 * value)
                                    else -> (sqrt(4.0 * value.pow(2.0) + 1.0) - 1.0) / (2.0 * value.pow(2.0))
                                }
                                Repository.addVar(key, VarGeometric(p))
                                print("→ p = ${Utility.format(p)}")
                            }
                            "Pois" -> {
                                val l = when(data[2])
                                {
                                    "E", "V" -> value
                                    else -> value.pow(2.0)
                                }
                                Repository.addVar(key, VarPoisson(l))
                                print("→ λ = ${Utility.format(l)}")
                            }
                            else -> {
                                val l = when(data[2])
                                {
                                    "E", "D" -> 1.0 / value
                                    else -> value.pow(-0.5)
                                }
                                Repository.addVar(key, VarExponential(l))
                                print("→ λ = ${Utility.format(l)}")
                            }
                        }
                        state = SolverState.INPUT
                    }
                    else if(state !== SolverState.INPUT)
                    {
                        throw InvalidExpressionException(line)
                    }
                }
                setSolver.reset()
            }
            if(Repository.isJointMode())
            {
                throw OpenTableException()
            }
        }
        catch(e: Exception)
        {
            when(e)
            {
                is VSException, is NumberFormatException -> ui.setOutput("[!] ${e.message}\n→ $lineCount. sor, $charCount. oszlop", Color.RED)
                else -> {
                    val stringWriter = StringWriter()
                    val printWriter = PrintWriter(stringWriter)
                    e.printStackTrace(printWriter)
                    ui.setOutput(stringWriter.toString(), Color.RED)
                }
            }
        }
        ui.setExecTime("Kész: %.3f ms".format((System.nanoTime() - timeBegin) * 1e-6))
        Repository.clear()
        return results
    }
}
