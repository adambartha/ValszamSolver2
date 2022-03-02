package exceptions

class InvalidPVarException(variable: String): VSException("Érvénytelen valószínűségi változó típus: '$variable'")
