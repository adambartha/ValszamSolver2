package variables

import core.Utility
import kotlin.math.pow

class VarBinomial(_n: Int, _p: Double): DVar()
{
    private val n = _n; private val p = _p; private val q = 1 - p
    override fun getMean(): Double = n.toDouble() * p
    override fun getVariance(): Double = n.toDouble() * p * q
    override fun getExact(x: Int): Double
    {
        return Utility.binomial(n, x).toDouble() * p.pow(x) * q.pow(n - x)
    }
    override fun getLessThan(x: Int): Double
    {
        return if (n > 60) Utility.deMoivreLaplace(this, x.toDouble()) else getAtMost(x - 1)
    }
    override fun getAtMost(x: Int): Double
    {
        return if (n > 60) Utility.deMoivreLaplace(this, x.toDouble()) else sum(x)
    }
    override fun getGreaterThan(x: Int): Double
    {
        return getAtLeast(x + 1)
    }
    override fun getAtLeast(x: Int): Double = sum(n)
}
