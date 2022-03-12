package variables

import kotlin.math.sqrt

sealed class PVar
{
    abstract fun getMean(): Double
    abstract fun getVariance(): Double
    fun getDeviation(): Double = sqrt(getVariance())
}
