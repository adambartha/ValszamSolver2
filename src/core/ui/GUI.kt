package core.ui

import core.SolverEngine
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.border.EtchedBorder
import javax.swing.JSpinner.DefaultEditor
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class GUI : JFrame(), IUserInterface
{
    private var split: JSplitPane
    private var inputPane: JScrollPane; private var outputPane: JScrollPane
    private lateinit var input: JTextArea; private lateinit var output: JTextArea
    private lateinit var execute: JButton
    private var precision: JSpinner; private var textSize: JSpinner
    private var fraction: JCheckBox; private var auto: JCheckBox
    private var execTime: JLabel

    init
    {
        val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        title = "ValszámSolver 2.0"
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(800, 600)
        size = Dimension(screen.displayMode.width / 2, screen.displayMode.height / 2)
        layout = BorderLayout()
        setLocationRelativeTo(null)
        isVisible = true

        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.CENTER, 10, 10)
        add(panel, BorderLayout.SOUTH)

        val caretLabel = JLabel("1, 1")
        caretLabel.preferredSize = Dimension(30, 20)
        panel.add(caretLabel)

        execTime = JLabel()
        execTime.preferredSize = Dimension(120, 30)
        execTime.horizontalAlignment = SwingConstants.CENTER
        execTime.border = EtchedBorder(EtchedBorder.LOWERED)
        panel.add(execTime)

        val textSizeLabel = JLabel("Betűméret:", SwingConstants.RIGHT)
        panel.add(textSizeLabel)
        textSize = JSpinner(SpinnerNumberModel(24, 12, 32, 1))

        val textSizeFT = (textSize.editor as DefaultEditor).textField
        textSizeFT.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        textSizeFT.horizontalAlignment = SwingConstants.CENTER
        textSize.addChangeListener {
            changeTextSize(input)
            changeTextSize(output)
        }
        panel.add(textSize)

        val precisionLabel = JLabel("Kerekítés:", SwingConstants.RIGHT)
        panel.add(precisionLabel)

        precision = JSpinner(SpinnerNumberModel(4, 1, 10, 1))
        val precisionFT = (precision.editor as DefaultEditor).textField
        precisionFT.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        precisionFT.horizontalAlignment = SwingConstants.CENTER
        precision.addChangeListener {
            update()
        }
        panel.add(precision)

        fraction = JCheckBox("Törtalak")
        fraction.horizontalTextPosition = SwingConstants.LEFT
        fraction.addActionListener {
            update()
        }
        panel.add(fraction)

        auto = JCheckBox("Automatikus számítás")
        auto.horizontalTextPosition = SwingConstants.LEFT
        auto.addActionListener {
            execute.isEnabled = !auto.isSelected
            update()
        }
        panel.add(auto)

        execute = JButton("Számítás")
        execute.addActionListener {
            SolverEngine.solve(fetchCommands())
        }
        panel.add(execute)

        input = JTextArea()
        changeTextSize(input)
        input.foreground = Color.BLACK
        input.document.addDocumentListener(object : DocumentListener
        {
            override fun insertUpdate(event: DocumentEvent?)
            {
                update()
            }
            override fun removeUpdate(event: DocumentEvent?)
            {
                update()
            }
            override fun changedUpdate(event: DocumentEvent?) {}
        })
        input.addCaretListener { event ->
            try
            {
                val position = event.dot
                val row = input.getLineOfOffset(position)
                val col = position - input.getLineStartOffset(row)
                caretLabel.text = String.format("%d, %d\n", row + 1, col + 1)
            }
            catch(_: Exception) {}
        }

        inputPane = JScrollPane(input)
        inputPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        output = JTextArea()
        changeTextSize(output)
        output.isEditable = false

        outputPane = JScrollPane(output)
        outputPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        split = JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPane, outputPane)
        split.border = EmptyBorder(10, 10, 0, 10)
        add(split, BorderLayout.CENTER)

        revalidate()
        repaint()
        addComponentListener(object : ComponentAdapter()
        {
            override fun componentResized(event: ComponentEvent)
            {
                resize()
            }
        })
        resize()
        execTime.text = "OK"
    }
    private fun resize()
    {
        split.setDividerLocation(0.5)
    }
    private fun changeTextSize(field: JTextArea)
    {
        field.font = Font(Font.MONOSPACED, Font.BOLD, textSize.value as Int)
    }
    private fun update()
    {
        if(auto.isSelected)
        {
            SolverEngine.solve(fetchCommands())
        }
    }

    override fun messageOut(message: String)
    {
        output.append("$message\n")
    }
    override fun getPrecision(): String
    {
        return precision.value.toString()
    }
    override fun isFractionEnabled(): Boolean
    {
        return fraction.isSelected
    }
    override fun fetchCommands(): ArrayList<String>
    {
        val commands = ArrayList<String>()
        for(line in input.text.replace(" ", "").split("\n"))
        {
            val lineInput = line.trim()
            if(lineInput.isNotBlank())
            {
                commands.add(lineInput)
            }
        }
        return commands
    }
    override fun setExecTime(text: String)
    {
        execTime.text = text
    }
    override fun setOutput(text: String?, color: Color?)
    {
        output.text = text
        if(color != null)
        {
            output.foreground = color
        }
    }
}
