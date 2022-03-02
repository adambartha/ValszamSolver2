package exceptions

class InvalidOperationException: VSException
{
    constructor(): super("Hibás értékadás vagy művelet")
    constructor(message: String): super("Hibás művelet: '$message")
}
