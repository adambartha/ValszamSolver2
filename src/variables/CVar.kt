package variables

abstract class CVar: PVar()
{
    abstract fun getCDF(t: Double): Double
    abstract fun getPDF(t: Double): Double
}
