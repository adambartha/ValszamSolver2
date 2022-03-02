package variables

import kotlin.math.sqrt

abstract class PVar
{
    abstract fun getMean(): Double
    abstract fun getVariance(): Double
    fun getDeviation(): Double
    {
        return sqrt(getVariance())
    }
}
