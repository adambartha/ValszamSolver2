package exceptions

class IncompleteBayesianException(event: String): VSException("Hiányos valószínűségi háló: hiányzik '$event'")
