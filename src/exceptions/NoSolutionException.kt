package exceptions

class NoSolutionException(message: String): VSException("Nincs megoldás: '$message'")
