package core.ui

import core.SolverEngine
import java.awt.Color

class CLI: IUserInterface
{
    private val commands = mutableListOf<String>()
    init
    {
        println("=== ${SolverEngine.getTitle().uppercase()} CLI ===\n")
    }
    override fun messageOut(message: String)
    {
        println(message)
    }
    override fun getPrecision(): String = "4"
    override fun isFractionEnabled(): Boolean = true
    override fun fetchCommands(): MutableList<String> = commands
    override fun setExecTime(text: String)
    {
        println("[$text]\n\n")
    }
    override fun setOutput(text: String?, color: Color?)
    {
        if(text == null)
        {
            return
        }
        println(text)
    }
    fun addCommand(input: String)
    {
        commands.add(input)
    }
}
