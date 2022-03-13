package variables

sealed class DVar: PVar()
{
    abstract fun getExact(x: Int): Double
    abstract fun getLessThan(x: Int): Double
    abstract fun getAtMost(x: Int): Double
    abstract fun getGreaterThan(x: Int): Double
    abstract fun getAtLeast(x: Int): Double
    protected fun sum(x: Int): Double
    {
        var result = 0.0
        for(i in 0..x)
        {
            result += getExact(i)
        }
        return result
    }
}
