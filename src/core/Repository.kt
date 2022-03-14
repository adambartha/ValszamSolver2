package core

import exceptions.*
import objects.*
import variables.*

object Repository
{
    private const val error = 1e-7
    private val vars: HashMap<String, PVar> = HashMap()
    private val jointVars: HashMap<String, JointProb> = HashMap()
    private val samples: HashMap<String, Sample> = HashMap()
    private val boxes: HashMap<String, Box> = HashMap()
    private val regions: HashMap<String, Region> = HashMap()
    private var jointMode: String? = null
    fun getError(): Double = error
    @Throws(InvalidNameException::class)
    fun validateName(key: String)
    {
        if(!key.matches(Regex("^[A-Za-z]+[0-9]*$")))
        {
            throw InvalidNameException(key)
        }
    }
    @Throws(InvalidNameException::class, ExistingVariableException::class)
    fun checkVariable(key: String)
    {
        validateName(key)
        if(hasVar(key) || getJointProbFromKey(key) != null || hasSample(key) || hasBox(key) || hasRegion(key))
        {
            throw ExistingVariableException(key)
        }
    }
    @Throws(InvalidNameException::class, UnknownVariableException::class)
    fun findVariable(key: String)
    {
        validateName(key)
        if(!hasVar(key) && getJointProbFromKey(key) == null && !hasSample(key) && !hasBox(key) && !hasRegion(key))
        {
            throw UnknownVariableException(key)
        }
    }
    fun clear()
    {
        vars.clear()
        jointVars.clear()
        samples.clear()
        boxes.clear()
        regions.clear()
    }
    @Throws(VSException::class)
    fun addVar(key: String, value: PVar)
    {
        checkVariable(key)
        vars[key] = value
    }
    fun getVar(key: String): PVar? = vars[key]
    fun hasVar(Key: String): Boolean = vars.containsKey(Key)
    @Throws(VSException::class)
    fun addJointVar(key: String, values: Array<String>)
    {
        jointVars[key] = JointProb(values)
        jointMode = key
    }
    @Throws(VSException::class)
    fun appendJointVar(input: String)
    {
        jointVars[jointMode!!]!!.add(input.split(',').toTypedArray())
    }
    fun getJointVar(key: String): JointProb? = jointVars[key]
    fun hasJointVar(key: String): Boolean = jointVars.containsKey(key)
    @Throws(VSException::class)
    fun closeJointVar()
    {
        jointVars[jointMode!!]!!.close()
        jointMode = null
    }
    fun getJointProbFromKey(key: String): JointProb?
    {
        for((hashKey, value) in jointVars)
        {
            val keys = hashKey.split(',')
            if(key == keys[0] || key == keys[1])
            {
                return value
            }
        }
        return null
    }
    fun isJointMode(): Boolean = jointMode != null
    fun addSample(key: String, sample: Sample)
    {
        samples[key] = sample
    }
    fun getSample(Key: String): Sample? = samples[Key]
    fun hasSample(key: String): Boolean = samples.containsKey(key)
    fun addBox(key: String, box: Box)
    {
        boxes[key] = box
    }
    fun getBox(key: String): Box? = boxes[key]
    fun hasBox(key: String): Boolean = boxes.containsKey(key)
    fun addRegion(key: String, region: Region)
    {
        regions[key] = region
    }
    fun getRegion(key: String): Region? = regions[key]
    fun hasRegion(key: String): Boolean = regions.containsKey(key)
}
