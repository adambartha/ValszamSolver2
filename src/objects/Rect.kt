package objects

import kotlin.math.abs

class Rect(x0: Double, y0: Double, x1: Double, y1: Double): Region()
{
    private val p0 = Point(x0, y0); private val p1 = Point(x1, y1)
    override fun getArea(): Double
    {
        return abs((p1.getX() - p0.getX()) * (p1.getY() - p0.getY()))
    }
}
