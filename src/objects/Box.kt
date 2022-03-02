package objects

import core.SolverEngine
import core.Utility

class Box
{
    private val good: Int; private val total: Int
    constructor(input: String)
    {
        val data = input.split(",").toTypedArray()
        good = data[0].toInt()
        total = good + data[1].toInt()
    }
    constructor(_good: Int, _bad: Int)
    {
        good = _good
        total = good + _bad
    }
    fun draw(withReplacement: Boolean)
    {
        SolverEngine.print("Összes: $total | Jó: $good | Rossz: ${total - good}")
        SolverEngine.print("Húzás ${if (withReplacement) "visszatevéssel" else "kivétellel"}")
        val p = DoubleArray(total)
        var max = 0.0
        var min = 1.0
        var i = 0
        var maxIndex = 0
        var minIndex = 0
        for(j in good..total)
        {
            val pi: Double = if (withReplacement) j.toDouble() / total.toDouble() else good.toDouble() / (total - i).toDouble()
            if(i > 0)
            {
                p[i - 1] = 1 - p[i - 1]
            }
            p[i] = pi
            var pX = 1.0
            var productString = ""
            for(k in 0..i)
            {
                pX *= p[k]
                productString += "${Utility.format(p[k])}${if (k < i) " * " else ""}"
            }
            i++
            if(pX > max)
            {
                max = pX
                maxIndex = i
            }
            else if (pX < min)
            {
                min = pX
                minIndex = i
            }
            SolverEngine.print("p$i = ${Utility.format(pi)} → P(X=$i) = $productString = ${Utility.format(pX)}")
        }
        SolverEngine.print("Maximum: P(X=$maxIndex) = ${Utility.format(max)}")
        SolverEngine.print("Minimum: P(X=$minIndex) = ${Utility.format(min)}")
    }
}
