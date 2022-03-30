package core.linear

import variables.PVar

class VarNode(_variable: PVar): ValueNode(1)
{
    private val variable = _variable
    fun getVariable(): PVar = variable
    override fun getValue(): Double = variable.getMean()
    override fun copy(): VarNode = VarNode(variable)
}
