package core.linear

import exceptions.SolverException

class OpNode(opType: OpType): LinearNode()
{
    private val op = opType
    private var left: LinearNode? = null
    private var right: LinearNode? = null
    @Throws(SolverException::class)
    fun set(node: LinearNode)
    {
        if(!hasLeft())
        {
            setLeft(node)
        }
        else if(!hasRight())
        {
            setRight(node)
        }
        else
        {
            throw SolverException("hibás kifejezés")
        }
    }
    fun getLeft(): LinearNode? = left
    fun setLeft(node: LinearNode)
    {
        left = node
    }
    fun hasLeft(): Boolean = left != null
    fun getRight(): LinearNode? = right
    fun setRight(node: LinearNode)
    {
        right = node
    }
    fun hasRight(): Boolean = right != null
    fun getOp(): OpType = op
    fun isOp(opType: OpType): Boolean = op == opType
    fun hasPrecedenceOver(opType: OpType): Boolean = op.hasPrecedenceOver(opType)
    fun flip()
    {
        val temp = right
        right = left
        left = temp
    }
    override fun copy(): OpNode = OpNode(op)
}
