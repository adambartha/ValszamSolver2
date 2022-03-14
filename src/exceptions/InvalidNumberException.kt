package exceptions

class InvalidNumberException(number: String): VSException("Hibás számformátum: '$number'")
