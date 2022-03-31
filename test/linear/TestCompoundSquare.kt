package linear

import core.Repository
import core.SolverEngine
import core.ui.CLI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TestCompoundSquare
{
    private val ui = CLI()
    @BeforeEach
    fun loadVariables()
    {
        SolverEngine.setUI(ui)
        ui.addCommand("X~Pois(1)")
        ui.addCommand("Y~Pois(2)")
        ui.addCommand("Z~Pois(3)")
    }
    @AfterEach
    fun clear()
    {
        Repository.clear()
    }
    @Test
    fun `(X+Y+Z)^2 = 42`() {
        ui.addCommand("?E((X+Y+Z)^2)")
        assertEquals(42.0, SolverEngine.solve(ui.fetchCommands())[0])
    }
    @Test
    fun `(X+Y-Z)^2 = 6`() {
        ui.addCommand("?E((X+Y-Z)^2)")
        assertEquals(6.0, SolverEngine.solve(ui.fetchCommands())[0])
    }
    @Test
    fun `(X-Y+Z)^2 = 10`() {
        ui.addCommand("?E((X-Y+Z)^2)")
        assertEquals(10.0, SolverEngine.solve(ui.fetchCommands())[0])
    }
    @Test
    fun `(X-Y-Z)^2 = 22`() {
        ui.addCommand("?E((X-Y-Z)^2)")
        assertEquals(22.0, SolverEngine.solve(ui.fetchCommands())[0])
    }
}
