package br.com.fenix.processatexto.model.enums

enum class Api(val descricao: String) {
    CONTA_PRINCIPAL("Google - Conta principal"),
    CONTA_SECUNDARIA("Google - Conta secundária"),
    CONTA_MIGRACAO_1("Google - Migracao 1"),
    CONTA_MIGRACAO_2("Google - Migracao 2"),
    CONTA_MIGRACAO_3("Google - Migracao 3"),
    CONTA_MIGRACAO_4("Google - Migracao 4"),
    API_GOOGLE("Api Google");

    // Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
    @Override
    override fun toString(): String {
        return descricao
    }
}