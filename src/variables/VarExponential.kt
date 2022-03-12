package variables

import kotlin.math.exp

class VarExponential(_l: Double): CVar()
{
    private val l = _l
    override fun getMean(): Double = 1.0 / l
    override fun getVariance(): Double = 1.0 / (l * l)
    override fun getCDF(t: Double): Double = 1.0 - exp(-l * t)
    override fun getPDF(t: Double): Double = l * exp(-l * t)
}
