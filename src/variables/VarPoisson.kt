package variables

import core.Utility
import kotlin.math.exp
import kotlin.math.pow

class VarPoisson(_l: Double): DVar()
{
    private val l = _l
    override fun getMean(): Double
    {
        return l
    }
    override fun getVariance(): Double
    {
        return l
    }
    override fun getExact(x: Int): Double
    {
        return l.pow(x) * exp(-l) / Utility.factorial(x).toDouble()
    }
    override fun getLessThan(x: Int): Double
    {
        return getAtMost(x - 1)
    }
    override fun getAtMost(x: Int): Double
    {
        var result = 0.0
        for(i in 0..x)
        {
            result += getExact(i)
        }
        return result
    }
    override fun getGreaterThan(x: Int): Double
    {
        return 1 - getAtMost(x)
    }
    override fun getAtLeast(x: Int): Double
    {
        return 1 - getAtMost(x - 1)
    }
}
