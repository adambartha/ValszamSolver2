package core.ui

import java.awt.Color

sealed interface IUserInterface
{
    fun messageOut(message: String)
    fun getPrecision(): String
    fun isFractionEnabled(): Boolean
    fun fetchCommands(): MutableList<String>
    fun setExecTime(text: String)
    fun setOutput(text: String?, color: Color?)
}
