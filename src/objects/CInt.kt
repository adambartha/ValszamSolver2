package objects

import core.Utility
import kotlin.math.sqrt

class CInt(_n: Int, _mean: Double, _dev: Double, _level: Double)
{
	private val n = _n; private val mean = _mean; private val dev = _mean
	private val level = _level; private val sig = 1.0 - level
	private val u = Utility.inversePhiFunction(getInverseU())
	private val c = u * dev / sqrt(n.toDouble())
	fun getSig(): Double = sig
	fun getU(): Double = u
	fun getInverseU(): Double = 1.0 - sig * 0.5
	fun getMin(): Double = mean - c
	fun getMax(): Double = mean + c
}
