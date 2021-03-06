package variables

import core.Utility
import kotlin.math.exp
import kotlin.math.pow

class VarPoisson(private val l: Double): DVar()
{
    override fun getMean(): Double = l
    override fun getVariance(): Double = l
    override fun getExact(x: Int): Double = l.pow(x) * exp(-l) / Utility.factorial(x).toDouble()
    override fun getLessThan(x: Int): Double = getAtMost(x - 1)
    override fun getAtMost(x: Int): Double = sum(x)
    override fun getGreaterThan(x: Int): Double = 1.0 - getAtMost(x)
    override fun getAtLeast(x: Int): Double = 1.0 - getAtMost(x - 1)
}
