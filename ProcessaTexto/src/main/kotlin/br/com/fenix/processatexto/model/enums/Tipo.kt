package br.com.fenix.processatexto.model.enums

enum class Tipo(val descricao: String) {
    TEXTO("Texto"), MUSICA("Música"), VOCABULARIO("Vocabulário"), KANJI("Kanji");

    @Override
    override fun toString(): String {
        return descricao
    }
}