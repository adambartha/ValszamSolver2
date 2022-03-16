package core.linear

import variables.PVar

class VarNode(_variable: PVar): LinearNode()
{
    private val variable = _variable
    fun getVariable(): PVar = variable
}
