package br.com.fenix.processatexto.model.enums

enum class Site(val descricao: String) {
    NENHUM("Nenhum"),
    TODOS("Todos"),
    JAPANESE_TANOSHI("Japanese tanoshi"),
    TANGORIN("Tangorin"),
    JAPANDICT("JapanDict"),
    JISHO("Jisho"),
    KANSHUDO("Kanshudo");

    // Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
    @Override
    override fun toString(): String {
        return descricao
    }
}