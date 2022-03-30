package core.linear

import exceptions.SolverException

class OpNode(opType: OpType): LinearNode(2)
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
        balance()
    }
    fun getLeft(): LinearNode? = left
    fun setLeft(node: LinearNode)
    {
        left = node
        node.setParent(this)
    }
    fun hasLeft(): Boolean = left != null
    fun getRight(): LinearNode? = right
    fun setRight(node: LinearNode)
    {
        right = node
        node.setParent(this)
    }
    fun hasRight(): Boolean = right != null
    fun isNotComplete(): Boolean = left == null || right == null
    fun getOp(): OpType = op
    fun isOp(opType: OpType): Boolean = op == opType
    fun hasPrecedenceOver(node: OpNode): Boolean = op.hasPrecedenceOver(node.getOp())
    fun hasNoPrecedenceOver(node: OpNode): Boolean = op.hasNoPrecedenceOver(node.getOp())
    private fun balance()
    {
        if(op != OpType.SUB && hasRight() && right!!.hasPriorityOver(left!!))
        {
            val temp = right
            right = left
            left = temp
        }
    }
    override fun copy(): OpNode = OpNode(op)
}
