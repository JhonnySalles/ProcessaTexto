package br.com.fenix.processatexto.model.enums

enum class Dicionario(val descricao: String) {
    SAMLL("Sudachi - small"), CORE("Sudachi - core"), FULL("Sudachi - full");

    @Override
    override fun toString(): String {
        return descricao
    }
}