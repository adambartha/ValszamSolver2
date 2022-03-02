package exceptions

class OutOfRangeException(variable: String): VSException("A megadott érték nem eleme: '$variable' értékkészletének")
