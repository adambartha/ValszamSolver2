package core.linear

import exceptions.*

private class OpNode(opType: OpType): LinearNode()
{
    private val op = opType
    private var left: LinearNode? = null
    private var right: LinearNode? = null
    @Throws(SolverException::class)
    override fun getValue(): Double
    {
        if(right == null || left == null)
        {
            throw SolverException("hiányos bináris kifejezés")
        }
        // TODO
        return 0.0
    }
}
