package core.linear

import core.Repository
import core.Utility
import exceptions.*
import java.lang.IllegalStateException
import java.util.ArrayDeque
import java.util.Stack
import kotlin.math.pow

class LinearSolver
{
    private var root: OpNode? = null
    @Throws(VSException::class)
    fun solve(input: String): Double
    {
        if(Utility.isNumeric(input))
        {
            return Utility.getValue(input)
        }
        if(Repository.hasVar(input))
        {
            return Repository.getVar(input)!!.getMean()
        }
        val jointProb = Repository.getJointProbFromKey(input)
        if(jointProb != null)
        {
            return jointProb.getMean(input)
        }
        var buffer = ""
        var parenCount = 0
        var node: LinearNode? = null
        for(char in input)
        {
            when(char)
            {
                '(' -> parenCount++
                ')' -> {
                    if(parenCount == 0)
                    {
                        throw InvalidExpressionException(input)
                    }
                    if(node is OpNode)
                    {
                        if(buffer.isEmpty())
                        {
                            throw InvalidExpressionException(input)
                        }
                        addNode(buffer, node)
                    }
                    parenCount--
                    buffer = ""
                }
                '+', '-', '*', '^' -> {
                    node = OpNode(when(char)
                    {
                        '+' -> OpType.ADD
                        '-' -> OpType.SUB
                        '*' -> OpType.MUL
                        '^' -> OpType.POW
                        else -> throw IllegalStateException()
                    })
                    if(root == null)
                    {
                        root = node
                    }
                    else
                    {
                        val rootNode = root!!
                        var pointer: LinearNode? = root
                        while(pointer is OpNode && pointer.hasRight())
                        {
                            pointer = pointer.getRight()
                        }
                        if(pointer is OpNode)
                        {
                            if(pointer.hasPrecedenceOver(node.getOp()))
                            {
                                addNode(buffer, pointer)
                                swapRoot(node, rootNode)
                                buffer = ""
                                continue
                            }
                            else
                            {
                                pointer.setRight(node)
                            }
                        }
                        else
                        {
                            swapRoot(node, rootNode)
                            continue
                        }
                    }
                    if(buffer.isEmpty())
                    {
                        throw InvalidExpressionException(input)
                    }
                    addNode(buffer, node)
                    buffer = ""
                }
                else -> buffer += char
            }
        }
        addNode(buffer, node as OpNode)
        val operators = ArrayDeque<OpNode>()
        val operands = ArrayDeque<LinearNode>()
        loadQueues(operators, operands)
        var steps = 0
        while(operators.isNotEmpty())
        {
            if(steps++ == 100)
            {
                throw SolverException("lépésszám-korlát")
            }
            val op = operators.poll()!!
            val left = operands.poll()!!
            val right = operands.poll()!!
            when(op.getOp())
            {
                OpType.POW -> {
                    val op2 = operators.poll()
                    if(op2 != null)
                    {
                        val power = operands.poll()
                        if(power !is ConstNode || power.getValue() != 2.0)
                        {
                            throw SolverException("túl bonyolult kifejezés")
                        }
                        var leftSquared = 1.0; var middle = 2.0; var rightSquared = 1.0
                        if(left is ConstNode)
                        {
                            leftSquared *= left.getValue().pow(2.0)
                            middle *= left.getValue()
                        }
                        else if(left is VarNode)
                        {
                            leftSquared *= left.getVariable().getSquaredMean()
                            middle *= left.getVariable().getMean()
                        }
                        if(right is ConstNode)
                        {
                            middle *= right.getValue()
                            rightSquared *= right.getValue().pow(2.0)
                        }
                        else if(right is VarNode)
                        {
                            middle *= right.getVariable().getMean()
                            rightSquared *= right.getVariable().getSquaredMean()
                        }
                        if(op2.getOp() == OpType.MUL)
                        {
                            operands.push(ConstNode(leftSquared * rightSquared))
                            continue
                        }
                        if(op2.getOp() == OpType.SUB)
                        {
                            middle *= -1.0
                        }
                        operands.push(ConstNode(leftSquared + middle + rightSquared))
                    }
                    else
                    {

                    }
                }
                OpType.MUL -> {
                    var leftValue = 0.0; var rightValue = 0.0
                    if(left is ConstNode)
                    {
                        leftValue = left.getValue()
                    }
                    else if(left is VarNode)
                    {
                        leftValue = left.getVariable().getMean()
                    }
                    if(right is ConstNode)
                    {
                        rightValue = right.getValue()
                    }
                    else if(right is VarNode)
                    {
                        rightValue = right.getVariable().getMean()
                    }
                    operands.push(ConstNode(leftValue * rightValue))
                }
            }
        }
        val result = operands.poll()
        if(result !is ConstNode || operands.isNotEmpty())
        {
            throw SolverException("sikertelen feloldás")
        }
        return result.getValue()
    }
    @Throws(VSException::class)
    private fun addNode(buffer: String, node: OpNode)
    {
        if(Utility.isNumeric(buffer))
        {
            val value = Utility.getValue(buffer)
            node.set(ConstNode(value))
        }
        else
        {
            if(buffer.first().isLetter())
            {
                val variable = Repository.getVar(buffer) ?: throw UnknownVariableException(buffer)
                node.set(VarNode(variable))
            }
            else
            {
                throw InvalidNameException(buffer)
            }
        }
    }
    private fun swapRoot(new: OpNode, old: LinearNode)
    {
        root = new
        new.setLeft(old)
    }
    private fun loadQueues(operators: ArrayDeque<OpNode>, operands: ArrayDeque<LinearNode>)
    {
        val stack = Stack<LinearNode>()
        stack.push(root)
        while(stack.isNotEmpty())
        {
            val pointer = stack.pop()!!
            if(pointer is OpNode)
            {
                operators.add(pointer)
                stack.push(pointer.getRight())
                stack.push(pointer.getLeft())
            }
            else
            {
                operands.add(pointer)
            }
        }
    }
}
