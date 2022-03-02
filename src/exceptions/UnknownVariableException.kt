package exceptions

class UnknownVariableException: VSException
{
    constructor(variable: String): super("Ismeretlen változó: '$variable'")
    constructor(variable1: String, variable2: String): super("Ismeretlen változók: '$variable1' és '$variable2'")
}
