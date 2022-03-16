package variables

import kotlin.math.pow
import kotlin.math.sqrt

sealed class PVar
{
    abstract fun getMean(): Double
    abstract fun getVariance(): Double
    fun getDeviation(): Double = sqrt(getVariance())
    fun getSquaredMean(): Double = getVariance() + getMean().pow(2.0)
}
