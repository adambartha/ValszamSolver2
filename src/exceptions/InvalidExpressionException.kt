package exceptions

class InvalidExpressionException(line: String): VSException("Hibás vagy hiányos utasítás: '$line'")
