package core.linear

class ConstNode(_value: Double): ValueNode(0)
{
    private val value = _value
    override fun getValue(): Double = value
    override fun copy(): ConstNode = ConstNode(value)
}
