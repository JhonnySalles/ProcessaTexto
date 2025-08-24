package br.com.fenix.processatexto.model.enums

enum class Igualdade(val valor : String) {
    IGUAL("="),
    MENOR("<"),
    MAIOR(">"),
    MENOR_IGUAL("<="),
    MAIOR_IGUAL(">="),
    DIFERENTE("!=");
}