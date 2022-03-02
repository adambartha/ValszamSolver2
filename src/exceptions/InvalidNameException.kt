package exceptions

class InvalidNameException(name: String): VSException("Hibás változónév: '$name'\nCsak betűvel kezdődhet és csak a végén lehet szám!")
