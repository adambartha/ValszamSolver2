package core.linear

sealed class LinearNode(_priority: Int)
{
    private val priority = _priority
    private var parent: OpNode? = null
    internal fun getPriority(): Int = priority
    internal fun hasPriorityOver(node: LinearNode): Boolean = priority > node.priority
    internal fun getParent(): OpNode? = parent
    internal fun setParent(node: OpNode)
    {
        parent = node
    }
    abstract fun copy(): LinearNode
}
