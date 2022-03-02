package exceptions

class ExistingVariableException(variable: String): VSException("Már létezik '$variable' változó")
