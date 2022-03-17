package core.linear

enum class OpType(private val order: Int)
{
    ADD(0),
    SUB(0),
    MUL(1),
    POW(2);
    fun hasPrecedenceOver(op: OpType): Boolean
    {
        return order > op.order
    }
}
