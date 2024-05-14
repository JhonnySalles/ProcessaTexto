package br.com.fenix.processatexto.model.entities.processatexto

data class Processar(
    var id: String = "",
    var original: String = "",
    var vocabulario: String = ""
) {
    @Override
    override fun toString(): String {
        return "Processar [id=$id, original=$original, vocabulario=$vocabulario]"
    }
}