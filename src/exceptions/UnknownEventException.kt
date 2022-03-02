package exceptions

class UnknownEventException: VSException
{
    constructor(event: String): super("Ismeretlen esemény: '$event'")
    constructor(event1: String, event2: String): super("Ismeretlen események: '$event1' és '$event2'")
}
