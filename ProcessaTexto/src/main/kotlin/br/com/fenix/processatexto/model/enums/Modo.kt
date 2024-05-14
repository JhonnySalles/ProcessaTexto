package br.com.fenix.processatexto.model.enums

enum class Modo(val descricao: String) {
    A("A - curto"), B("B - médio"), C("C - longo");

    @Override
    override fun toString(): String {
        return descricao
    }
}