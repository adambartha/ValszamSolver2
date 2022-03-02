package exceptions

class InvalidValueException(value: String): VSException("Érvénytelen érték: '$value'")
