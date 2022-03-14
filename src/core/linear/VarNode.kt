package core.linear

import variables.PVar

private class VarNode(_variable: PVar): LinearNode()
{
    private val variable = _variable
    override fun getValue(): Double = variable.getMean()
}
