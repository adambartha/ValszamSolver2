package core.linear

import core.Repository
import core.SolverEngine
import core.Utility
import exceptions.*
import java.lang.IllegalStateException
import java.util.ArrayDeque

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
                '(' -> {
                    if(buffer.isNotEmpty())
                    {
                        throw InvalidExpressionException(input)
                    }
                    parenCount++
                }
                ')' -> {
                    if(parenCount == 0)
                    {
                        throw InvalidExpressionException(input)
                    }
                    if(node == null)
                    {
                        if(Utility.isNumeric(buffer))
                        {
                            return Utility.getValue(buffer)
                        }
                        if(Repository.hasVar(buffer))
                        {
                            return Repository.getVar(buffer)!!.getMean()
                        }
                        else
                        {
                            throw UnknownVariableException(buffer)
                        }
                    }
                    else if(node is OpNode)
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
                                if(pointer.getLeft() is ConstNode)
                                {
                                    pointer.flip()
                                }
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
        loadQueues(root!!, operators, operands)
        var steps = 0
        while(operators.isNotEmpty())
        {
            if(steps++ == SolverEngine.getStepLimit())
            {
                throw SolverException("lépésszám-korlát")
            }
            val op = operators.poll()!!
            val left = operands.poll()!!
            val right = operands.poll()!!
            when(op.getOp())
            {
                OpType.POW -> {
                    if(left is VarNode)
                    {
                        if(right !is ConstNode || right.getValue() != 2.0)
                        {
                            throw SolverException("túl bonyolult kifejezés")
                        }
                        operands.add(ConstNode(left.getVariable().getSquaredMean()))
                    }
                    else
                    {
                        // TODO
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
                else -> {
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
                    if(op.isOp(OpType.SUB))
                    {
                        rightValue *= -1.0
                    }
                    operands.push(ConstNode(leftValue + rightValue))
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
        val left = node.getLeft()
        if(node.isOp(OpType.POW) && left is OpNode)
        {
            if(left.isOp(OpType.POW) || !Utility.isNumeric(buffer))
            {
                throw SolverException("túl bonyolult")
            }
            if(!left.isOp(OpType.MUL))
            {
                root = expand(left)
                return
            }
        }
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
                if(node.getLeft() !is VarNode)
                {
                    node.flip()
                }
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
    private fun loadQueues(node: LinearNode, operators: ArrayDeque<OpNode>, operands: ArrayDeque<LinearNode>)
    {
        if(node is OpNode)
        {
            loadQueues(node.getLeft()!!, operators, operands)
            loadQueues(node.getRight()!!, operators, operands)
            operators.add(node)
        }
        else
        {
            operands.add(node)
        }
    }
    private fun expand(node: OpNode): OpNode
    {
        val left = node.getLeft() as VarNode; val right = node.getRight() as ConstNode
        val newRoot = OpNode(OpType.ADD); val diff = node.copy()
        val mul0 = OpNode(OpType.MUL); val mul1 = OpNode(OpType.MUL)
        val power = OpNode(OpType.POW)
        newRoot.setLeft(diff)
        diff.setRight(mul0)
        mul0.setLeft(ConstNode(2.0))
        mul0.setRight(mul1)
        mul1.setLeft(left.copy())
        mul1.setRight(right.copy())
        power.setLeft(left.copy())
        power.setRight(ConstNode(2.0))
        diff.setLeft(power)
        newRoot.setRight(ConstNode(right.getValue() * right.getValue()))
        return newRoot
    }
}
