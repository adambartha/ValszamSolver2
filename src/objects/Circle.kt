package objects

import kotlin.math.PI

class Circle(x: Double, y: Double, _r: Double): Region()
{
    private val p = Point(x, y)
    private val r = _r
    override fun getArea(): Double
    {
        return r * r * PI
    }
}
