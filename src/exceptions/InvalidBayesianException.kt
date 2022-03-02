package exceptions

class InvalidBayesianException(link: String): VSException("Hibás valószínűségi háló kapcsolat: '$link'")
