package br.com.fenix.processatexto.model.entities

open class Novel(
    var base: String = "",
    open var novel: String = "",
    open var volume: Float = 0f,
    open var capitulo: Float = 0f,
    var texto: String = "",
    var isProcessar: Boolean = false,
    var linguagem: String = "",
    var isAlterado: Boolean = false,
    var isItemExcluido: Boolean = false,
    var prefixo: String = "",
    var origem: String = ""
) {

    constructor(base: String, novel: String, linguagem: String) : this(base, novel) {
        this.linguagem = linguagem
    }

    constructor(novel: String, volume: Float, capitulo: Float) : this("", novel, volume, capitulo) {  }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Novel

        if (base != other.base) return false
        if (novel != other.novel) return false
        if (linguagem != other.linguagem) return false

        return true
    }

    override fun hashCode(): Int {
        var result = base?.hashCode() ?: 0
        result = 31 * result + (novel?.hashCode() ?: 0)
        result = 31 * result + (linguagem?.hashCode() ?: 0)
        return result
    }

}