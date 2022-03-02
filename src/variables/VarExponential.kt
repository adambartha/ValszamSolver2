package variables

import kotlin.math.exp

class VarExponential(_l: Double): CVar()
{
    private val l = _l
    override fun getMean(): Double
    {
        return 1 / l
    }
    override fun getVariance(): Double
    {
        return 1 / (l * l)
    }
    override fun getCDF(t: Double): Double
    {
        return 1 - exp(-l * t)
    }
    override fun getPDF(t: Double): Double
    {
        return l * exp(-l * t)
    }
}
