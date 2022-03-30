package core.linear

enum class OpType(private val order: Int)
{
    ADD(0),
    SUB(1),
    MUL(2),
    POW(3);
    fun hasPrecedenceOver(op: OpType): Boolean = order > op.order
    fun hasNoPrecedenceOver(op: OpType): Boolean = order < op.order
}
