package exceptions

class ResolveException(type: String): VSException("Nem feloldható változó típus: '$type'")
