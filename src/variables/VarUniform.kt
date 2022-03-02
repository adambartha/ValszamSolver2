package variables

import kotlin.math.pow

class VarUniform(_a: Double, _b: Double): CVar()
{
    private val a = _a.coerceAtMost(_b); private val b = _a.coerceAtLeast(_b)
    override fun getMean(): Double
    {
        return (a + b) * 0.5
    }
    override fun getVariance(): Double
    {
        return (b - a).pow(2.0) / 12.0
    }
    override fun getCDF(t: Double): Double
    {
        return (t - a) / (b - a)
    }
    override fun getPDF(t: Double): Double
    {
        return 1.0 / (b - a)
    }
}
