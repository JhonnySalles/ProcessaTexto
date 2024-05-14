package br.com.fenix.processatexto.model.exceptions

class DatabaseException(mensagem: String) : Exception(mensagem) {
    companion object {
        private const val serialVersionUID = 1L
    }
}