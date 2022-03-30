package core.linear

sealed class ValueNode(_priority: Int) : LinearNode(_priority)
{
    abstract fun getValue(): Double
}
