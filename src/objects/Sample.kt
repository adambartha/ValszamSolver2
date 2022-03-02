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
        val data = input.split(",").toTypedArray()
        set = DoubleArray(data.size)
        var sum = 0.0
        for (i in 0 until getN()) {
            set[i] = Utility.getValue(data[i])
            sum += set[i]
        }
        mean = sum / getN().toDouble()
    }

    fun getN(): Int
    {
        return set.size
    }
    fun getMean(): Double
    {
        return mean
    }
    fun getEmp(): Double
    {
        var sum = 0.0
        for(value in set)
        {
            sum += (value - mean).pow(2.0)
        }
        return sum / getN().toDouble()
    }
    fun getEmpCorr(): Double
    {
        var sum = 0.0
        for(value in set)
        {
            sum += (value - mean).pow(2.0)
        }
        return sum / (getN() - 1).toDouble()
    }
    fun getDeviation(): Double
    {
        return sqrt(getEmpCorr())
    }
    fun getCDF(x: Double): Double
    {
        var sum = 0.0
        for(value in set)
        {
            if(value < x)
            {
                sum += 1.0
            }
        }
        return sum / getN().toDouble()
    }
}
