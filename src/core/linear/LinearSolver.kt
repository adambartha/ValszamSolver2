package core.linear

import core.Repository
import core.Utility
import exceptions.*
import kotlin.math.pow

class LinearSolver
{
    private var root: OpNode? = null
    private var pointer: OpNode? = null
    private var buffer = ""
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
        Repository.getJointProbFromKey(input)?.let { return it.getMean(input) }
        var parenCount = 0
        val groupStack = mutableListOf<OpNode>()
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
                    if(pointer == null)
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
                    else if(buffer.isNotEmpty())
                    {
                        insertNode(pointer!!)
                    }
                    parenCount--
                }
                '+', '-', '*', '^' -> {
                    val op = pointer
                    pointer = OpNode(when(char)
                    {
                        '+' -> OpType.ADD
                        '-' -> OpType.SUB
                        '*' -> OpType.MUL
                        else -> OpType.POW
                    })
                    if(groupStack.isNotEmpty())
                    {
                        if(groupStack.size == parenCount && pointer!!.hasNoPrecedenceOver(groupStack.last()))
                        {
                            groupStack[groupStack.size - 1] = pointer!!
                        }
                        else if(groupStack.size > parenCount && groupStack.last() == op!!)
                        {
                            overrideOperator(op)
                            continue
                        }
                    }
                    else if(parenCount > 0)
                    {
                        groupStack.add(pointer!!)
                    }
                    if(root == null)
                    {
                        root = pointer
                        if(buffer.isEmpty())
                        {
                            throw InvalidExpressionException(input)
                        }
                        insertNode(pointer!!)
                    }
                    else
                    {
                        op!!
                        if(groupStack.size > parenCount)
                        {
                            val groupOp = groupStack.removeLast()
                            if(pointer!!.hasPrecedenceOver(groupOp))
                            {
                                overrideOperator(groupOp)
                            }
                            else
                            {
                                op.set(pointer!!)
                                insertNode(pointer!!)
                            }
                        }
                        else if(pointer!!.hasPrecedenceOver(op) && pointer != groupStack.last() && !pointer!!.isOp(OpType.SUB)
                            || pointer!!.hasNoPrecedenceOver(op) && pointer == groupStack.last())
                        {
                            overrideOperator(op)
                        }
                        else
                        {
                            if(op.isNotComplete())
                            {
                                op.set(pointer!!)
                                insertNode(pointer!!)
                            }
                            else
                            {
                                overrideOperator(op)
                                groupStack[groupStack.size - 1] = pointer!!
                            }
                        }
                    }
                }
                else -> buffer += char
            }
        }
        insertNode(pointer!!)
        return evaluate(root!!)
    }
    @Throws(VSException::class)
    private fun insertNode(parent: OpNode)
    {
        val left = parent.getLeft()
        if(parent.isOp(OpType.POW) && left is OpNode)
        {
            if(left.isOp(OpType.POW) || !Utility.isNumeric(buffer))
            {
                throw SolverException("túl bonyolult")
            }
            val expandedRoot = expand(left)
            val parentOfParent = parent.getParent()
            if(parentOfParent == null)
            {
                root = expandedRoot
            }
            else
            {
                if(parentOfParent.isOp(OpType.SUB) && parentOfParent.getRight() == parent)
                {
                    parentOfParent.setRight(expandedRoot)
                }
                else
                {
                    parentOfParent.setLeft(expandedRoot)
                }
            }
            return
        }
        parseBuffer(parent)
    }
    @Throws(VSException::class)
    private fun overrideOperator(node: OpNode)
    {
        val parent = node.getParent()
        if(parent == null)
        {
            root = pointer
            root!!.setLeft(node)
            if(buffer.isNotEmpty() && node.isNotComplete())
            {
                parseBuffer(node)
            }
            return
        }
        if(parent.getLeft() == node)
        {
            parent.setLeft(pointer!!)
        }
        else
        {
            parent.setRight(pointer!!)
        }
        pointer!!.setLeft(node)
        if(buffer.isEmpty())
        {
            return
        }
        parseBuffer(node)
    }
    @Throws(VSException::class)
    private fun parseBuffer(node: OpNode)
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
        buffer = ""
    }
    private fun expand(node: OpNode, value: ValueNode? = null): OpNode
    {
        val left = node.getLeft()!!; val right = node.getRight()!!
        if(node.isOp(OpType.POW))
        {
            throw SolverException("túl bonyolult")
        }
        if(node.isOp(OpType.MUL))
        {
            val op = node.copy()
            if(left is VarNode)
            {
                op.setLeft(createVarSquared(left))
            }
            if(right is ConstNode)
            {
                op.setRight(ConstNode(right.getValue().pow(2.0)))
            }
            return op
        }
        val add = OpNode(OpType.ADD)
        add.setRight(when(right)
        {
            is OpNode -> expand(right, if (left is ValueNode) left else null)
            is VarNode -> createVarSquared(right)
            else -> ConstNode((right as ConstNode).getValue().pow(2.0))
        })
        when(left)
        {
            is OpNode -> add.setLeft(expand(left, if (right is ValueNode) right else null))
            is VarNode -> {
                val pow = createVarSquared(left)
                if(right is ValueNode)
                {
                    val op = node.copy()
                    add.setLeft(op)
                    val mul = createMidTerm(left, right)
                    if(value == null)
                    {
                        op.setLeft(pow)
                        op.setRight(mul)
                    }
                    else
                    {
                        val parent: OpNode = node.getParent()!!
                        val op2 = if (node.isOp(OpType.SUB) && parent.isOp(OpType.SUB)) OpNode(OpType.ADD) else op.copy()
                        op2.setLeft(pow)
                        op2.setRight(mul)
                        val mulRight = createMidTerm(right, value)
                        op.setRight(mulRight)
                        val mulLeft = createMidTerm(left, value)
                        val op3 = parent.copy()
                        op3.setLeft(op2)
                        op3.setRight(mulLeft)
                        op.setLeft(op3)
                    }
                }
                else
                {
                    add.setLeft(pow)
                }
            }
            else -> add.setRight(ConstNode((left as ConstNode).getValue().pow(2.0)))
        }
        return add
    }
    private fun createVarSquared(node: VarNode): OpNode
    {
        val pow = OpNode(OpType.POW)
        pow.setLeft(node.copy())
        pow.setRight(ConstNode(2.0))
        return pow
    }
    private fun createMidTerm(left: ValueNode, right: ValueNode): OpNode
    {
        val mul0 = OpNode(OpType.MUL); val mul1 = mul0.copy()
        mul0.setLeft(mul1)
        mul0.setRight(ConstNode(2.0))
        mul1.setLeft(left.copy())
        mul1.setRight(right.copy())
        return mul0
    }
    private fun evaluate(node: LinearNode): Double
    {
        if(node is ValueNode)
        {
            return node.getValue()
        }
        node as OpNode
        val leftNode = node.getLeft()!!
        val rightNode = node.getRight()!!
        if(node.isOp(OpType.POW) && leftNode is VarNode)
        {
            if(rightNode is ConstNode && rightNode.getValue() == 2.0)
            {
                return leftNode.getVariable().getSquaredMean()
            }
            throw SolverException("túl bonyolult")
        }
        val left = evaluate(leftNode)
        val right = evaluate(rightNode)
        return when(node.getOp())
        {
            OpType.ADD -> left + right
            OpType.SUB -> left - right
            OpType.MUL -> left * right
            else -> left.pow(right)
        }
    }
}
