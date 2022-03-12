package objects

import kotlin.math.pow
import kotlin.math.sqrt

class Point(_x: Double, _y: Double)
{
    private val x = _x; private val y = _y
    fun getX(): Double = x
    fun getY(): Double = y
    fun getDistanceFrom(p: Point): Double
    {
        return sqrt((x - p.x).pow(2.0) + (y - p.y).pow(2.0))
    }
}
