package br.com.fenix.processatexto.model.enums

enum class Tela(val descricao: String) {
    TEXTO("Texto"), LEGENDA("Legenda"), MANGA("Manga");

    // Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
    @Override
    override fun toString(): String {
        return descricao
    }
}