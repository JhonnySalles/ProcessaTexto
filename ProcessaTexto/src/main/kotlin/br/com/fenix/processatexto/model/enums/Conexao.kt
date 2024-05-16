package br.com.fenix.processatexto.model.enums


enum class Conexao(private val value: String, val isExternal : Boolean) {
    PROCESSA_TEXTO("PROCESSA_TEXTO", false),
    MANGA_EXTRACTOR("MANGA_EXTRACTOR", false),
    NOVEL_EXTRACTOR("NOVEL_EXTRACTOR", false),
    TEXTO_INGLES("TEXTO_INGLES", false),
    TEXTO_JAPONES("TEXTO_JAPONES", false),
    DECKSUBTITLE("DECKSUBTITLE", false),
    FIREBASE("FIREBASE", true);

    @Override
    override fun toString(): String {
        return value
    }

}