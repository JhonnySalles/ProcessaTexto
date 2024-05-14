package br.com.fenix.processatexto.model.enums

enum class Conexao(private val value: String) {
    PROCESSA_TEXTO("PROCESSA_TEXTO"),
    MANGA_EXTRACTOR("MANGA_EXTRACTOR"),
    NOVEL_EXTRACTOR("NOVEL_EXTRACTOR"),
    TEXTO_INGLES("TEXTO_INGLES"),
    TEXTO_JAPONES("TEXTO_JAPONES"),
    DECKSUBTITLE("DECKSUBTITLE"),
    FIREBASE("FIREBASE");

    @Override
    override fun toString(): String {
        return value
    }
}