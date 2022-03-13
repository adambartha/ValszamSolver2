package objects

import core.Utility
import kotlin.math.pow
import kotlin.math.sqrt

class Sample(input: String)
{
    private val set: DoubleArray
    private val mean: Double
    init
    {
        val data = input.split(',')
        set = DoubleArray(data.size)
        var sum = 0.0
        for(i in 0 until getN()) {
            set[i] = Utility.getValue(data[i])
            sum += set[i]
        }
        mean = sum / getN().toDouble()
    }
    fun getN(): Int = set.size
    fun getMean(): Double = mean
    fun getEmp(): Double = emp(getN())
    fun getEmpCorr(): Double = emp(getN() - 1)
    fun getDeviation(): Double = sqrt(getEmpCorr())
    fun getCDF(x: Double): Double
    {
        var sum = 0.0
        set.forEach { value -> if (value < x) sum += 1.0 }
        return sum / getN().toDouble()
    }
    private fun emp(n: Int): Double
    {
        var sum = 0.0
        set.forEach { value -> sum += (value - mean).pow(2.0) }
        return sum / n.toDouble()
    }
}
