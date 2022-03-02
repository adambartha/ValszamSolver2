package core

import kotlin.math.pow

object Combinatorics
{
    fun permutation(n: Int, k: IntArray): Long
    {
        val num = Utility.factorial(n)
        if(k.isNotEmpty())
        {
            var den = 1L
            for(kVal in k)
            {
                den *= Utility.factorial(kVal)
            }
            return num / den
        }
        return num
    }
    fun combination(repeated: Boolean, n: Int, k: Int): Long
    {
        return Utility.binomial(if (repeated) n + k - 1 else n, k)
    }
    fun variation(repeated: Boolean, n: Int, k: Int): Long
    {
        if(repeated)
        {
            return n.toDouble().pow(k.toDouble()).toLong()
        }
        var result = 1L
        for(i in n - k + 1..n)
        {
            result *= i.toLong()
        }
        return result
    }
}
