package objects

abstract class Region
{
    abstract fun getArea(): Double
    fun getProbOf(r: Region): Double
    {
        return getArea() / r.getArea()
    }
}
