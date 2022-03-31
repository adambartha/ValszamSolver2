package core

import exceptions.*
import java.util.*
import kotlin.math.abs

class SetSolver
{
    private val vars = mutableMapOf<String, Double>()
    private val bayesianNetwork = mutableListOf<String>()
    private var steps = 0
    fun reset()
    {
        steps = 0
    }
    @Throws(VSException::class)
    fun add(key: String, value: Double)
    {
        if(value > 1 || value < 0)
        {
            throw InvalidValueException("valószínűségi mérték nem lehet negatív vagy 1-nél nagyobb")
        }
        val conditionIndex = key.indexOf('|')
        if(conditionIndex >= 0 && conditionIndex != key.lastIndexOf('|'))
        {
            throw InvalidParameterException(key)
        }
        var prev: Char? = null
        var parenCount = 0
        for((charIndex, char) in key.toCharArray().withIndex())
        {
            if(char == '/')
            {
                if(prev != null && prev !in "()+*|,")
                {
                    throw InvalidParameterException(key)
                }
            }
            else if(char == '(')
            {
                if(prev != null && prev !in "(+*/")
                {
                    throw InvalidParameterException(key)
                }
                parenCount++
            }
            else if(char == '+' || char == '*' || char == ')' || char == '|')
            {
                if(prev == null || !(prev.isLetterOrDigit() || prev == ')'))
                {
                    throw InvalidParameterException(key)
                }
                if(char == ')')
                {
                    if(parenCount == 0)
                    {
                        throw InvalidParameterException(key)
                    }
                    parenCount--
                }
            }
            else if(char == ',')
            {
                if(charIndex > conditionIndex && prev != null && !prev.isLetterOrDigit())
                {
                    throw InvalidParameterException(key)
                }
            }
            else if(char.isLetterOrDigit())
            {
                if(prev != null && prev !in "(+*/|," && !prev.isLetterOrDigit())
                {
                    throw InvalidParameterException(key)
                }
            }
            else
            {
                throw InvalidCharacterException(char)
            }
            prev = char
        }
        if(parenCount > 0 || !(prev!!.isLetterOrDigit() || prev == ')'))
        {
            throw InvalidParameterException(key)
        }
        if(conditionIndex >= 0)
        {
            val left: String = Utility.getOrderedKey(key.substring(0, conditionIndex))
            val right: String = Utility.getOrderedKey(key.substring(conditionIndex + 1))
            val orderedKey = "$left|$right"
            vars[orderedKey] = value
            if('+' !in left && '*' !in left)
            {
                vars[if (orderedKey.startsWith("/")) orderedKey.substring(1) else "/$orderedKey"] = 1 - value
            }
        }
        else if('*' !in key && '+' !in key)
        {
            vars[key] = value
            vars[if (key.startsWith("/")) key.substring(1) else "/$key"] = 1 - value
        }
        else if('*' in key)
        {
            if('+' in key)
            {
                vars[key] = value
            }
            else
            {
                val parts = key.split('*').toTypedArray()
                parts.sort()
                vars[parts.joinToString("*")] = value
            }
        }
        else
        {
            val parts = key.split('+').toTypedArray()
            parts.sort()
            vars[parts.joinToString("+")] = value
        }
    }
    @Throws(VSException::class)
    fun makeIndependent(a: String, b: String)
    {
        var pA = vars[a]
        var pB = vars[b]
        if(pA == null && pB == null)
        {
            throw UnknownEventException(a, b)
        }
        val pAB: Double?
        val key = Utility.getJointKey(a, b, '*')
        if(pA != null && pB != null)
        {
            pAB = pA * pB
            if(vars.containsKey(key) && abs(vars[key]!! - pAB) > Repository.getError())
            {
                throw SolverException("már adott P($a ∩ $b) ≠ P($a)P($b)")
            }
            vars[key] = pAB
            vars[Utility.getJointKey(a, "/$b", '*')] = pA * (1 - pB)
            vars[Utility.getJointKey("/$a", b, '*')] = (1 - pA) * pB
            vars[Utility.getJointKey("/$a", "/$b", '*')] = (1 - pA) * (1 - pB)
            SolverEngine.print("$a és $b függetlenek: P($a ∩ $b) = P($a)P($b) = ${Utility.format(pAB)}")
        }
        else
        {
            pAB = vars[key]
            if(pAB == null)
            {
                throw SolverException("P($a ∩ $b) nincs megadva (helytelen függetlenség)")
            }
            val errorMessage = "%s és %s nem lehetnek függetlenek, mert %s > %s"
            if(pA == null)
            {
                if(pAB > pB!!)
                {
                    throw SolverException(errorMessage.format(a, b, Utility.format(pAB), Utility.format(pB)))
                }
                pA = pAB / pB
                vars[a] = pA
                vars["/$a"] = 1 - pA
            }
            else
            {
                if(pAB > pA)
                {
                    throw SolverException(errorMessage.format(a, b, Utility.format(pAB), Utility.format(pA)))
                }
                pB = pAB / pA
                vars[b] = pB
                vars["/$b"] = 1 - pB
            }
        }
    }
    @Throws(VSException::class)
    fun makeExclusive(a: String, b: String)
    {
        var pA = vars[a]
        var pB = vars[b]
        if(pA == null && pB == null)
        {
            throw UnknownEventException(a, b)
        }
        val pAB: Double?
        val keyUnion = Utility.getJointKey(a, b, '+')
        val keyIntersection = Utility.getJointKey(a, b, '*')
        vars[keyIntersection] = 0.0
        if(pA != null && pB != null)
        {
            pAB = pA + pB
            if(pAB > 1)
            {
                throw SolverException("P($a ∪ $b) = P($a) + P($b) > 1")
            }
            if(vars.containsKey(keyUnion) && abs(vars[keyUnion]!! - pAB) > Repository.getError())
            {
                throw SolverException("már adott P($a ∪ $b) ≠ P($a) + P($b)")
            }
            vars[keyUnion] = pAB
            vars[Utility.getJointKey(a, "/$b", '+')] = 1 - pB
            vars[Utility.getJointKey("/$a", b, '+')] = 1 - pA
            vars[Utility.getJointKey(a, "/$b", '*')] = pA
            vars[Utility.getJointKey("/$a", b, '*')] = pB
            vars[Utility.getJointKey("/$a", "/$b", '*')] = 1 - pAB
            SolverEngine.print("$a és $b kizárják egymást: $a ∩ $b = ∅ → P($a ∪ $b) = P($a) + P($b) = ${Utility.format(pAB)}")
        }
        else
        {
            val intersection = vars[keyIntersection]
            if(intersection != null && intersection > 0)
            {
                throw SolverException("$a és $b nem zárhatják ki egymást, mert már adott P($a ∩ $b) > 0")
            }
            pAB = vars[keyUnion]
            if(pAB == null)
            {
                throw SolverException("P($a ∪ $b) nincs megadva (helytelen kizárás)")
            }
            val errorMessage = "%s és %s nem zárhatják ki egymást, mert %s < %s"
            if(pA == null)
            {
                if(pAB < pB!!)
                {
                    throw SolverException(errorMessage.format(a, b, Utility.format(pAB), Utility.format(pB)))
                }
                pA = pAB - pB
                vars[a] = pA
                vars["/$a"] = 1 - pA
            }
            else
            {
                if(pAB < pA)
                {
                    throw SolverException(errorMessage.format(a, b, Utility.format(pAB), Utility.format(pA)))
                }
                pB = pAB - pA
                vars[b] = pB
                vars["/$b"] = 1 - pB
            }
        }
    }
    fun hasKey(key: String): Boolean
    {
        return vars.containsKey(key)
    }
    @Throws(VSException::class)
    fun areIndependent(a: String, b: String): Boolean
    {
        val pA = vars[a] ?: throw UnknownEventException(a)
        val pB = vars[b] ?: throw UnknownEventException(b)
        val pAB = vars[Utility.getJointKey(a, b, '*')] ?: return false
        return pAB == pA * pB
    }
    @Throws(VSException::class)
    fun areExclusive(a: String, b: String): Boolean
    {
        val pA = vars[a] ?: throw UnknownEventException(a)
        val pB = vars[b] ?: throw UnknownEventException(b)
        val intersection = vars[Utility.getJointKey(a, b, '*')]
        val union = vars[Utility.getJointKey(a, b, '+')]
        return if (intersection == null || union == null) false else (intersection == 0.0 && union == pA + pB)
    }
    @Throws(VSException::class)
    fun addBayesianLink(input: String)
    {
        val parts = input.split("->").toTypedArray()
        for(i in bayesianNetwork.indices)
        {
            val linkParts = bayesianNetwork[i].split("->").toTypedArray()
            for(j in 0..1)
            {
                if(mergeLink(1 - j, i, parts, linkParts))
                {
                    return
                }
            }
        }
        bayesianNetwork.add(input)
    }
    private fun mergeLink(i: Int, j: Int, parts: Array<String>, linkParts: Array<String>): Boolean
    {
        if(linkParts[1 - i] == parts[1 - i])
        {
            val nodes = ArrayList<String>()
            for(node in linkParts[i].split(','))
            {
                nodes.add(node)
            }
            val n = nodes.size
            for(node in parts[i].split(','))
            {
                if(node !in nodes)
                {
                    nodes.add(node)
                }
            }
            if(nodes.size == n)
            {
                return true
            }
            val updateNodes = nodes.toTypedArray()
            updateNodes.sort()
            val update = updateNodes.joinToString(",")
            if(linkParts[i] != update)
            {
                bayesianNetwork[j] = "$update->${parts[1 - i]}"
                return true
            }
        }
        return false
    }
    @Throws(VSException::class)
    fun solve(input: String): Double
    {
        if(steps++ == SolverEngine.getStepLimit())
        {
            throw SolverException("lépésszám-korlát")
        }
        var index: Int
        if(input.indexOf("/(").also { index = it } > -1)
        {
            val before = input.substring(0, index)
            val begin = index + 2
            while(index < input.length && input[index] != ')')
            {
                index++
            }
            val expression = input.substring(begin, index)
            val parts = expression.split(Regex("(?<=[+*])|(?=[+*])")).toTypedArray()
            for(i in parts.indices)
            {
                if(parts[i] == "*")
                {
                    parts[i] = "+"
                }
                else if(parts[i] == "+")
                {
                    parts[i] = "*"
                }
                else if(parts[i].startsWith("/"))
                {
                    parts[i] = parts[i].substring(1)
                }
                else
                {
                    parts[i] = "/${parts[i]}"
                }
            }
            val inverseExpression = parts.joinToString("")
            val formatString = if ('*' in inverseExpression) "(%s)" else "%s"
            val remainder = input.substring(index + 1)
            val next = "$before${formatString.format(inverseExpression)}$remainder"
            SolverEngine.print("De-Morgan azonosság: ${Utility.toSetNotation("$expression = $inverseExpression")}")
            SolverEngine.print(Utility.toSetNotation("$input = $next"))
            return solve(next)
        }
        else if(input.indexOf('|').also { index = it } > -1)
        {
            val left = Utility.getOrderedKey(input.substring(0, index))
            val right = Utility.getOrderedKey(input.substring(index + 1))
            val key = "$left|$right"
            if(vars.containsKey(key))
            {
                return vars[key]!!
            }
            val reverseKey = "$right|$left"
            val reverseValue = vars[reverseKey]
            if(reverseValue != null)
            {
                val n = reverseValue * vars[left]!!
                val bayesKey = left.replace(Regex("[0-9]+$"), "")
                if(vars.containsKey(bayesKey + "1"))
                {
                    var value: Double
                    var actual: String
                    var message = "P($left|$right) = P($right|$left)P($left) / ["
                    var i = 1
                    var d = 0.0
                    while(vars[bayesKey + i.toString().also { actual = it }].also { value = it!! } != null)
                    {
                        val actualValue = vars["$right|$actual"] ?: throw SolverException("P($right | $actual) nincs megadva")
                        d += actualValue * value
                        i++
                        message += "P($right|$actual)P($actual) + "
                    }
                    message = message.substring(0, message.length - 3) + "]"
                    SolverEngine.print("Bayes-tétel:\n$message")
                    return n / d
                }
                val jointKey = Utility.getJointKey(left, right, '*')
                val jointNegatedKey = Utility.getJointKey("/$left", right, '*')
                SolverEngine.print("P(${Utility.toSetNotation(jointKey)}) = P(${Utility.toSetNotation(reverseKey)})P($left) = ${Utility.format(n)}")
                val inverseKey = String.format("%s|/%s", right, left)
                val inverseValue = solve(inverseKey) * vars["/$left"]!!
                SolverEngine.print("P(${Utility.toSetNotation(jointNegatedKey)}) = P(${Utility.toSetNotation(inverseKey)})P(/$left) = ${Utility.format(inverseValue)}")
                val rightValue = n + inverseValue
                SolverEngine.print("P($right) = P(${Utility.toSetNotation(jointKey)}) + P(${Utility.toSetNotation(jointNegatedKey)}) = ${Utility.format(rightValue)}")
                return n / rightValue
            }
            if('+' in left)
            {
                val subIndex = left.indexOf('+')
                val subLeft: String = Utility.extract(left.substring(0, subIndex))
                val subRight: String = Utility.extract(left.substring(subIndex + 1))
                val n1 = solve("($subLeft)*($right)")
                val n2 = solve("($subRight)*($right)")
                val n3 = solve("($subLeft)*($subRight)*($right)")
                val d = solve(right)
                SolverEngine.print("P(${Utility.toSetNotation(input)}) = P($subLeft | $right) + P($subRight | $right) - P($subLeft ∩ $subRight | $right)")
                SolverEngine.print("P($subLeft | $right) = P($subLeft ∩ $right) / P($right) = ${Utility.format(n1 / d)}")
                SolverEngine.print("P($subRight | $right) = P($subRight ∩ $right) / P($right) = ${Utility.format(n2 / d)}")
                SolverEngine.print("P($subLeft ∩ $subRight | $right) = P($subLeft ∩ $subRight ∩ $right) / P($right) = ${Utility.format(n3 / d)}")
                return (n1 + n2 - n3) / d
            }
            if(bayesianNetwork.isNotEmpty())
            {
                val conditions = right.split('*').toTypedArray()
                val nodes = getBayesianNodes()
                if(conditions.size == nodes.size - 1)
                {
                    val n = solveBayesian("$left*$right")
                    return n / (n + solveBayesian("${Utility.negate(left)}*$right"))
                }
                val missingNodes = ArrayList<String>()
                for(node in nodes)
                {
                    if(node != Utility.makePositive(left))
                    {
                        var termFound = false
                        for(term in conditions)
                        {
                            if(node == Utility.makePositive(term))
                            {
                                termFound = true
                                break
                            }
                        }
                        if(!termFound)
                        {
                            missingNodes.add(node)
                            SolverEngine.print("$node esemény nem szerepel a feltételben → beszámítandó még $node és /$node")
                        }
                    }
                }
                var n = 0.0
                for(p in 0..1)
                {
                    var terms = "$left*$right"
                    missingNodes.forEach { term -> terms += "*${if (p == 0) term else "/$term"}" }
                    n += solveBayesian(terms)
                }
                var d = 0.0
                for(p in 0..1)
                {
                    var terms = "${Utility.negate(left)}*$right"
                    missingNodes.forEach { term -> terms += "*${if (p == 0) term else "/$term"}" }
                    d += solveBayesian(terms)
                }
                return n / (n + d)
            }
            return solve("($left)*($right)") / solve(right)
        }
        else if(input.indexOf('*').also { index = it } > -1)
        {
            val left = Utility.extract(input.substring(0, index))
            val right = Utility.extract(input.substring(index + 1))
            if(left.isBlank() || right.isBlank())
            {
                throw SolverException("hiányos megadás")
            }
            val key = "$left*$right"
            var value = vars[key]
            if(value != null)
            {
                val leftKey = Utility.makePositive(left)
                val rightKey = Utility.makePositive(right)
                if(areIndependent(leftKey, rightKey) && (Utility.isNegated(left) || Utility.isNegated(right)))
                {
                    SolverEngine.print(Utility.toSetNotation("$leftKey és $rightKey függetlenek, így a komplementerek is: P($key) = P($left)P($right)"))
                }
                return value!!
            }
            if ('*' in left || '*' in right)
            {
                val parts = input.split('*').toTypedArray()
                for(i in 0 until parts.size - 1)
                {
                    for(j in i + 1 until parts.size)
                    {
                        val subLeft = Utility.extract(parts[i])
                        val subRight = Utility.extract(parts[j])
                        val subKey = "$subLeft*$subRight"
                        value = vars[subKey]
                        if(value != null)
                        {
                            val simplified: String
                            val formatString = "Kizárás miatt %1\$s ⊂ %2\$s → $subKey = %2\$s → $input = %3\$s"
                            if(value == vars[subLeft])
                            {
                                simplified = Utility.getSimplified(parts, j, "*")
                                SolverEngine.print(Utility.toSetNotation(Utility.removeParentheses(formatString.format(subRight, subLeft, simplified))))
                                return solve(simplified)
                            }
                            if(value == vars[subRight])
                            {
                                simplified = Utility.getSimplified(parts, i, "*")
                                SolverEngine.print(Utility.toSetNotation(Utility.removeParentheses(formatString.format(subLeft, subRight, simplified))))
                                return solve(simplified)
                            }
                        }
                    }
                }
            }
            val negatedLeft = Utility.isNegated(left); val negatedRight = Utility.isNegated(right)
            val leftIntersection = '*' in left; val rightIntersection = '*' in right
            if(!negatedLeft && negatedRight && !leftIntersection)
            {
                return solve(left) - solve(Utility.getJointKey(left, right.substring(1), '*'))
            }
            if(negatedLeft && !negatedRight && !rightIntersection)
            {
                return solve(right) - solve(Utility.getJointKey(left.substring(1), right, '*'))
            }
            val leftFormat = if (leftIntersection) "(%s)" else "%s"
            val rightFormat = if (rightIntersection) "(%s)" else "%s"
            val unionKey = "$leftFormat+$rightFormat".format(left, right)
            if(!vars.containsKey(unionKey))
            {
                throw SolverException("P(${Utility.toSetNotation(unionKey)}) nincs megadva")
            }
            val resultLeft = solve(left)
            val resultRight = solve(right)
            val resultUnion = solve(unionKey)
            val result = resultLeft + resultRight - resultUnion
            SolverEngine.print("P(${Utility.toSetNotation(input)}) = P($left) + P($right) - P(${Utility.toSetNotation(unionKey)}) = ${Utility.format(resultLeft)} + ${Utility.format(resultRight)} - ${Utility.format(resultUnion)} = ${Utility.format(result)}")
            return result
        }
        else if(input.indexOf('+').also { index = it } > -1)
        {
            val left = Utility.extract(input.substring(0, index))
            val right = Utility.extract(input.substring(index + 1))
            if(left.isBlank() || right.isBlank())
            {
                throw SolverException("hiányos megadás")
            }
            val key = "$left+$right"
            var value = vars[key]
            if(value != null)
            {
                return value!!
            }
            if('*' !in input)
            {
                val parts = input.split('+').toTypedArray()
                for(i in 0 until parts.size - 1)
                {
                    for (j in i + 1 until parts.size)
                    {
                        val subLeft: String = Utility.extract(parts[i])
                        val subRight: String = Utility.extract(parts[j])
                        val subKey = "$subLeft+$subRight"
                        value = vars[subKey]
                        if(value != null)
                        {
                            val simplified: String
                            val formatString = "Kizárás miatt %1\$s ⊂ %2\$s → $subKey = %2\$s → $input = %3\$s"
                            if(value == vars[subLeft])
                            {
                                simplified = Utility.getSimplified(parts, j, "+")
                                SolverEngine.print(Utility.toSetNotation(Utility.removeParentheses(formatString.format(subRight, subLeft, simplified))))
                                return solve(simplified)
                            }
                            if(value == vars[subRight])
                            {
                                simplified = Utility.getSimplified(parts, i, "+")
                                SolverEngine.print(Utility.toSetNotation(Utility.removeParentheses(formatString.format(subLeft, subRight, simplified))))
                                return solve(simplified)
                            }
                        }
                    }
                }
            }
            val leftFormat = if ('+' in left) "(%s)" else "%s"
            val rightFormat = if ('+' in right) "(%s)" else "%s"
            val intersection = "$leftFormat*$rightFormat".format(left, right)
            if(!vars.containsKey(intersection))
            {
                throw SolverException("P(${Utility.toSetNotation(intersection)}) nincs megadva")
            }
            val resultLeft = solve(left)
            val resultRight = solve(right)
            val resultIntersection = solve(intersection)
            val result = resultLeft + resultRight - resultIntersection
            SolverEngine.print("P(${Utility.toSetNotation(input)}) = P($left) + P($right) - P(${Utility.toSetNotation(intersection)}) = ${Utility.format(resultLeft)} + ${Utility.format(resultRight)} - ${Utility.format(resultIntersection)} = ${Utility.format(result)}")
            return result
        }
        else
        {
            if(!vars.containsKey(input))
            {
                throw UnknownEventException(input)
            }
            return vars[input]!!
        }
    }
    @Throws(VSException::class)
    private fun solveBayesian(input: String): Double
    {
        var result = 1.0
        var output = ""
        val terms = input.split('*', '|')
        val found = mutableMapOf<String, Boolean>()
        for(term in terms)
        {
            found[Utility.makePositive(term)] = false
        }
        val processed = ArrayList<String>()
        for(link in bayesianNetwork)
        {
            val parts = link.split("->").toTypedArray()
            val parents = parts[0].split(',').toTypedArray()
            val children = parts[1].split(',').toTypedArray()
            for(i in parents.indices)
            {
                var termFound = false
                for(term in terms)
                {
                    val key = Utility.makePositive(term)
                    if(key == parents[i])
                    {
                        parents[i] = term
                        termFound = true
                        found[key] = true
                        break
                    }
                }
                if(!termFound)
                {
                    throw UnknownEventException(parents[i])
                }
            }
            for(i in children.indices)
            {
                var termFound = false
                for(term in terms)
                {
                    val key = Utility.makePositive(term)
                    if(key == children[i])
                    {
                        children[i] = term
                        termFound = true
                        found[key] = true
                        break
                    }
                }
                if(!termFound)
                {
                    throw UnknownEventException(children[i])
                }
            }
            if(parents.size > 1)
            {
                for(parent in parents)
                {
                    if(parent !in processed)
                    {
                        result *= solve(parent)
                        output += "P($parent)"
                        processed.add(parent)
                    }
                }
                val parentsInput = parents.joinToString("*")
                result *= solve("${children[0]}|$parentsInput")
                output += "P(${children[0]} | ${Utility.toSetNotation(parentsInput)})"
            }
            else
            {
                for(child in children)
                {
                    result *= solve("$child|${parents[0]}")
                    output += "P($child | ${parents[0]})"
                }
            }
        }
        for((key, value) in found)
        {
            if(!value)
            {
                throw IncompleteBayesianException(key)
            }
        }
        SolverEngine.print("P(${Utility.toSetNotation(input)}) = $output = ${Utility.format(result)}")
        return result
    }
    private fun getBayesianNodes(): Array<String>
    {
        val terms = ArrayList<String>()
        for(link in bayesianNetwork)
        {
            val parts = link.split("->")
            for(parent in parts[0].split(','))
            {
                if(parent !in terms)
                {
                    terms.add(parent)
                }
            }
            for(child in parts[1].split(','))
            {
                if(child !in terms)
                {
                    terms.add(child)
                }
            }
        }
        val nodes = terms.toTypedArray()
        nodes.sort()
        return nodes
    }
}
