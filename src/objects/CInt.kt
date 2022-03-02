package objects

import core.Utility
import kotlin.math.sqrt

class CInt(_n: Int, _mean: Double, _dev: Double, _level: Double)
{
	private val n = _n; private val mean = _mean; private val dev = _mean
	private val level = _level; private val sig = 1 - level
	private val u = Utility.inversePhiFunction(getInverseU())
	private val c = u * dev / sqrt(n.toDouble())
	fun getSig(): Double
	{
		return sig
	}
	fun getU(): Double
	{
		return u
	}
	fun getInverseU(): Double
	{
		return 1.0 - sig * 0.5
	}
	fun getMin(): Double
	{
		return mean - c
	}
	fun getMax(): Double
	{
		return mean + c
	}
}
