package exceptions

class InvalidParameterException(parameter: String): VSException("Érvénytelen paraméter: '$parameter'")
