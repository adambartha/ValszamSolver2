package variables

import core.Utility
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.PI

class VarNormal(private val m: Double, private val v: Double): CVar()
{
    override fun getMean(): Double = m
    override fun getVariance(): Double = v
    override fun getCDF(t: Double): Double = Utility.phiFunction(t, m, getDeviation())
    override fun getPDF(t: Double): Double = exp(-(t - m).pow(2.0) / (2.0 * v)) / sqrt(2.0 * PI * v)
}
