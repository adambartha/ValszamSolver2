package core.linear

import core.Repository
import core.Utility
import exceptions.*

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
                        else -> throw java.lang.IllegalStateException()
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
        // TODO IMPLEMENT DFS EVALUATION
        return 0.0
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
}
