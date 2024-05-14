package br.com.fenix.processatexto.model.enums

enum class Driver(val descricao: String) {
    MYSQL("MYSQL"), EXTERNO("EXTERNO");

    override fun toString(): String {
        return descricao
    }
}