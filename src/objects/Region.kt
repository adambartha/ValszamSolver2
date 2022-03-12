package objects

sealed class Region
{
    abstract fun getArea(): Double
    fun getProbOf(r: Region): Double = getArea() / r.getArea()
}
