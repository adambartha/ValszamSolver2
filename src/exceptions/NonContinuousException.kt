package exceptions

class NonContinuousException(variable: String): VSException("'$variable' nem folytonos eloszlású")
