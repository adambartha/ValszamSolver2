package core.linear

class ConstNode(_value: Double): LinearNode()
{
    private val value = _value
    override fun getValue(): Double = value
}
