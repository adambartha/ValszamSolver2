package core

import exceptions.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class JointProb(params: Array<String>)
{
    private val x = params[0]; private val y = params[1]
    private val xVal: DoubleArray = DoubleArray(params.size - 2)
    {
        i -> Utility.getValue(params[i + 2])
    }
    private val pX: DoubleArray = DoubleArray(params.size - 2) { 0.0 }
    private val yVal = ArrayList<Double>()
    private val joint = ArrayList<DoubleArray>()
    private val pY = ArrayList<Double>()
    fun add(data: Array<String>)
    {
        yVal.add(Utility.getValue(data[0]))
        joint.add(DoubleArray(xVal.size) { 0.0 })
        val line = joint.last()
        var p = 0.0
        for(i in line.indices)
        {
            line[i] = Utility.getValue(data[i + 1])
            p += line[i]
        }
        pY.add(p)
    }
    @Throws(VSException::class)
    fun close()
    {
        for(i in pY.indices)
        {
            for(j in pX.indices)
            {
                pX[j] += joint[i][j]
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
        for(i in yVal.indices)
        {
            for(j in xVal.indices)
            {
                result += xVal[j] * yVal[i] * joint[i][j]
            }
        }
        return result
    }
    @Throws(VSException::class)
    fun getMean(key: String): Double
    {
        var result = 0.0
        when(key)
        {
            x -> {
                for(i in pX.indices)
                {
                    result += pX[i] * xVal[i]
                }
            }
            y -> {
                for(i in pY.indices)
                {
                    result += pY[i] * yVal[i]
                }
            }
            else -> throw UnknownVariableException(key)
        }
        return result
    }
    @Throws(VSException::class)
    fun getVariance(key: String): Double
    {
        var result = 0.0
        if(key == x)
        {
            for(i in pX.indices)
            {
                result += pX[i] * xVal[i].pow(2.0)
            }
            return result - getMean(x).pow(2.0)
        }
        if(key == y)
        {
            for(i in pY.indices)
            {
                result += pY[i] * yVal[i].pow(2.0)
            }
            return result - getMean(y).pow(2.0)
        }
        throw UnknownVariableException(key)
    }
    @Throws(VSException::class)
    fun getCov(): Double
    {
        return getMean() - getMean(x) * getMean(y)
    }
    @Throws(VSException::class)
    fun getCorr(): Double
    {
        return getCov() / sqrt(getVariance(x) * getVariance(y))
    }
    @Throws(VSException::class)
    fun getMarginal(key: String, value: Double): Double
    {
        if(key == x)
        {
            for(i in xVal.indices)
            {
                if(xVal[i] == value)
                {
                    return pX[i]
                }
            }
            throw OutOfRangeException(x)
        }
        if(key == y)
        {
            for(i in yVal.indices)
            {
                if(yVal[i] == value)
                {
                    return pY[i]
                }
            }
            throw OutOfRangeException(y)
        }
        throw UnknownVariableException(key)
    }
}
