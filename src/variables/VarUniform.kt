package variables

import kotlin.math.pow

class VarUniform(private val a: Double, private val b: Double): CVar()
{
    override fun getMean(): Double = (a + b) * 0.5
    override fun getVariance(): Double = (b - a).pow(2.0) / 12.0
    override fun getCDF(t: Double): Double = (t - a) / (b - a)
    override fun getPDF(t: Double): Double = 1.0 / (b - a)
}
