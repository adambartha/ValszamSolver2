package variables

import kotlin.math.pow

class VarGeometric(_p: Double): DVar()
{
    private val p = _p; private val q = 1 - p
    override fun getMean(): Double
    {
        return 1 / p
    }
    override fun getVariance(): Double
    {
        return q / (p * p)
    }
    override fun getExact(x: Int): Double
    {
        return p * getAtLeast(x)
    }
    override fun getLessThan(x: Int): Double
    {
        return 1 - getAtLeast(x)
    }
    override fun getAtMost(x: Int): Double
    {
        return 1 - getGreaterThan(x)
    }
    override fun getGreaterThan(x: Int): Double
    {
        return q.pow(x)
    }
    override fun getAtLeast(x: Int): Double
    {
        return getGreaterThan(x - 1)
    }
}
