package br.com.fenix.processatexto.model.enums

enum class Database(private val modo: String) {
    INGLES("INGLES"), JAPONES("JAPONES"), EXTERNO("EXTERNO");

    @Override
    override fun toString(): String {
        return modo
    }
}