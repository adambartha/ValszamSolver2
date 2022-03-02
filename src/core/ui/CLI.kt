package core.ui

import java.awt.Color

class CLI: IUserInterface
{
    private val commands = ArrayList<String>()
    init {
        println("=== VALSZÁMSOLVER 2.0 CLI ===\n")
    }
    override fun messageOut(message: String)
    {
        println(message)
    }
    override fun getPrecision(): String
    {
        return "4"
    }
    override fun isFractionEnabled(): Boolean
    {
        return true
    }
    override fun fetchCommands(): ArrayList<String>
    {
        return commands
    }
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
