package variables

import kotlin.math.pow

class VarGeometric(private val p: Double): DVar()
{
    private val q = 1.0 - p
    override fun getMean(): Double = 1.0 / p
    override fun getVariance(): Double = q / (p * p)
    override fun getExact(x: Int): Double = p * getAtLeast(x)
    override fun getLessThan(x: Int): Double = 1.0 - getAtLeast(x)
    override fun getAtMost(x: Int): Double = 1.0 - getGreaterThan(x)
    override fun getGreaterThan(x: Int): Double = q.pow(x)
    override fun getAtLeast(x: Int): Double = getGreaterThan(x - 1)
}
