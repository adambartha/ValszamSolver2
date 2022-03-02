package variables

import core.Utility
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.PI

class VarNormal(_m: Double, _v: Double): CVar()
{
    private val m = _m; private val v = _v
    override fun getMean(): Double
    {
        return m
    }
    override fun getVariance(): Double
    {
        return v
    }
    override fun getCDF(t: Double): Double
    {
        return Utility.phiFunction(t, m, getDeviation())
    }
    override fun getPDF(t: Double): Double
    {
        return exp(-(t - m).pow(2.0) / (2.0 * v)) / sqrt(2 * PI * v)
    }
}
