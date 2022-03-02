package objects

import kotlin.math.sqrt

class Triangle(x0: Double, y0: Double, x1: Double, y1: Double, x2: Double, y2: Double): Region()
{
    private val p0 = Point(x0, y0); private val p1 = Point(x1, y1); private val p2 = Point(x2, y2)
    override fun getArea(): Double
    {
        val a = p0.getDistanceFrom(p1)
        val b = p1.getDistanceFrom(p2)
        val c = p2.getDistanceFrom(p0)
        val s = (a + b + c) * 0.5
        return sqrt(s * (s - a) * (s - b) * (s - c))
    }
}
