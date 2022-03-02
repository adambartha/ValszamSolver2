package exceptions

class SolverException(message: String): VSException("Számítási hiba: '$message'")
