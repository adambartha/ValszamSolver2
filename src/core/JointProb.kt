package core

import exceptions.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class JointProb(params: Array<String>)
{
    private val x = params[0]; private val y = params[1]
    private val xVal = DoubleArray(params.size - 2)
    {
        i -> Utility.getValue(params[i + 2])
    }
    private val pX = DoubleArray(params.size - 2) { 0.0 }
    private val yVal = mutableListOf<Double>()
    private val joint = mutableListOf<DoubleArray>()
    private val pY = mutableListOf<Double>()
    fun add(data: Array<String>)
    {
        yVal.add(Utility.getValue(data[0]))
        joint.add(DoubleArray(xVal.size) { 0.0 })
        val line = joint.last()
        var p = 0.0
        line.indices.forEach {
            line[it] = Utility.getValue(data[it + 1])
            p += line[it]
        }
        pY.add(p)
    }
    @Throws(VSException::class)
    fun close()
    {
        for(y in pY.indices)
        {
            for(x in pX.indices)
            {
                pX[x] += joint[y][x]
            }
        }
        if(abs(pX.sum() - 1.0) > Repository.getError() || abs(pY.sum() - 1.0) > Repository.getError())
        {
            throw JointProbabilityException()
        }
    }
    fun getMean(): Double
    {
        var result = 0.0
        for(y in yVal.indices)
        {
            for(x in xVal.indices)
            {
                result += xVal[x] * yVal[y] * joint[y][x]
            }
        }
        return result
    }
    @Throws(UnknownVariableException::class)
    fun getMean(key: String): Double
    {
        var result = 0.0
        when(key)
        {
            x -> pX.indices.forEach { result += pX[it] * xVal[it] }
            y -> pY.indices.forEach { result += pY[it] * yVal[it] }
            else -> throw UnknownVariableException(key)
        }
        return result
    }
    @Throws(UnknownVariableException::class)
    fun getVariance(key: String): Double
    {
        var result = 0.0
        when(key)
        {
            x -> pX.indices.forEach { result += pX[it] * xVal[it].pow(2.0) }
            y -> pY.indices.forEach { result += pY[it] * yVal[it].pow(2.0) }
            else -> throw UnknownVariableException(key)
        }
        return result
    }
    @Throws(VSException::class)
    fun getCov(): Double = getMean() - getMean(x) * getMean(y)
    @Throws(VSException::class)
    fun getCorr(): Double = getCov() / sqrt(getVariance(x) * getVariance(y))
    @Throws(VSException::class)
    fun getMarginal(key: String, value: Double): Double
    {
        when(key)
        {
            x -> {
                for(i in xVal.indices)
                {
                    if(xVal[i] == value)
                    {
                        return pX[i]
                    }
                }
                throw OutOfRangeException(x)
            }
            y -> {
                for(i in yVal.indices)
                {
                    if(yVal[i] == value)
                    {
                        return pY[i]
                    }
                }
                throw OutOfRangeException(y)
            }
            else -> throw UnknownVariableException(key)
        }
    }
}
