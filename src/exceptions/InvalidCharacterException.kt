package exceptions

class InvalidCharacterException(character: Char): VSException("Érvénytelen karakter: '$character'")
