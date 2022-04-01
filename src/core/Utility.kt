package core

import java.util.*
import kotlin.math.*
import variables.*

object Utility
{
    fun binomial(n: Int, k: Int): Long
    {
        var result = 1L
        for(i in 0 until k)
        {
            result = result * (n - i) / (i + 1)
        }
        return result
    }
    fun factorial(n: Int): Long
    {
        var result = 1L
        for(i in 2L..n.toLong())
        {
            result *= i
        }
        return result
    }
    fun isNumeric(value: String): Boolean
    {
        if(value.isEmpty() || "-/.".any { value.indexOf(it) != value.lastIndexOf(it) })
        {
            return false
        }
        val first = value.first()
        return value.all { it.isDigit() || it in "-/." } && (first == '-' || first.isDigit())
    }
    fun getValue(value: String): Double
    {
        val parts = value.split('/')
        return if (parts.size == 1) parts[0].toDouble() else parts[0].toDouble() / parts[1].toDouble()
    }
    fun deMoivreLaplace(variable: PVar, t: Double): Double
    {
        SolverEngine.print("De Moivre-Laplace tétel: [X - E(X)] / σ(X) közelítőleg normális eloszlású")
        return VarNormal(variable.getMean(), variable.getVariance()).getCDF(t)
    }
    fun errorFunction(z: Double): Double
    {
        val t0 = 1.0 / (1.0 + 0.5 * abs(z))
        val p = doubleArrayOf(
            -1.26551223, 1.00002368, 0.37409196, 0.09678418, -0.18628806,
            0.27886807, -1.13520398, 1.48851587, -0.82215223, 0.17087277
        )
        var q = 0.0
        for(i in p.indices)
        {
            q += p[i] * t0.pow(i.toDouble())
        }
        val t1 = t0 * exp(q - z * z)
        return if (z < 0) t1 - 1.0 else 1.0 - t1
    }
    fun inverseErrorFunction(z: Double): Double
    {
        return when(z)
        {
            0.0 -> z
            else -> {
                val a = 0.147
                val p0 = ln(1.0 - z * z)
                val p1 = p0 * 0.5 + 2.0 / (PI * a)
                val p2 = sqrt(p1 * p1 - p0 / a)
                sign(z) * sqrt(p2 - p1)
            }
        }
    }
    fun phiFunction(x: Double, m: Double, d: Double): Double
    {
        return 0.5 * (1.0 + errorFunction((x - m) / (d * sqrt(2.0))))
    }
    fun inversePhiFunction(p: Double): Double
    {
        return sqrt(2.0) * inverseErrorFunction(2.0 * p - 1.0)
    }
    fun getJointKey(a: String, b: String, operator: Char): String
    {
        val keyA = a.replaceFirst("/", "")
        val keyB = b.replaceFirst("/", "")
        return if (keyA > keyB) "$b$operator$a" else "$a$operator$b"
    }
    fun removeParentheses(input: String): String
    {
        return input.replace("(", "").replace(")", "")
    }
    fun extract(input: String): String
    {
        operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)
        return when(input)
        {
            in Regex("^\\([^()]+\\)$") -> input.substring(1, input.length - 1)
            in Regex("^\\([^()]+") -> input.drop(1)
            in Regex("[^()]+\\)$") -> input.dropLast(1)
            else -> input
        }
    }
    fun isWhole(n: Double): Boolean = abs(n.roundToInt() - n) < Repository.getError()
    fun format(n: Double): String
    {
        if(isWhole(n))
        {
            return "${n.roundToInt()}"
        }
        if(abs(n - PI) < Repository.getError())
        {
            return "π"
        }
        if(abs(n - E) < Repository.getError())
        {
            return "e"
        }
        if(SolverEngine.getUI().isFractionEnabled())
        {
            for(i in 2..10_000)
            {
                val multiplier = n * i.toDouble()
                val number = multiplier.roundToInt()
                if(isWhole(multiplier))
                {
                    return if (number == i) "1" else "$number/$i"
                }
                if(isWhole(multiplier / PI))
                {
                    val out = "π/$i"
                    return when(val scalar = (multiplier / PI).roundToInt())
                    {
                        1 -> out
                        -1 -> "-$out"
                        else -> "$scalar$out"
                    }
                }
                if(isWhole(multiplier / E))
                {
                    val out = "e/$i"
                    return when(val scalar = (multiplier / E).roundToInt())
                    {
                        1 -> out
                        -1 -> "-$out"
                        else -> "$scalar$out"
                    }
                }
            }
        }
        return String.format("%.${SolverEngine.getUI().getPrecision()}f", n)
    }
    fun getSimplified(input: Array<String>, index: Int, operator: String): String
    {
        val next = mutableListOf<String>()
        for(i in input.indices)
        {
            if(i != index)
            {
                next.add(input[i])
            }
        }
        return next.toTypedArray().joinToString(operator)
    }
    fun toSetNotation(input: String): String
    {
        return input.replace("*", " ∩ ").replace("+", " ∪ ").replace("|", " | ")
    }
    fun isNegated(input: String): Boolean = input.matches(Regex("^/\\w+$"))
    fun makePositive(input: String): String = if (isNegated(input)) input.drop(1) else input
    fun negate(input: String): String = if (isNegated(input)) input.drop(1) else "/$input"
    fun getOrderedKey(input: String): String
    {
        val key = extract(input)
        val hasIntersection = '*' in key
        val hasUnion = '+' in key
        return if(hasIntersection && !hasUnion)
        {
            order(key, "*")
        }
        else if(!hasIntersection && hasUnion)
        {
            order(key, "+")
        }
        else key
    }
    private fun order(input: String, operator: String): String
    {
        val terms = TreeMap<String, Boolean>()
        for(term in input.split(operator))
        {
            terms[makePositive(term)] = !isNegated(term)
        }
        val events = mutableListOf<String>()
        for(event in terms.keys)
        {
            events.add(if (terms[event]!!) event else "/$event")
        }
        return events.joinToString(operator)
    }
}
