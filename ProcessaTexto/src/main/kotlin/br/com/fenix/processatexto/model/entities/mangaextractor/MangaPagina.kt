package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import com.google.gson.annotations.Expose
import java.util.*


data class MangaPagina(
    var id: UUID? = null,
    @Expose override var nomePagina: String = "",
    @Expose var numero: Int = 0,
    @Expose var hash: String = "",
    @Expose var textos: MutableList<MangaTexto> = mutableListOf(),
    @Expose var vocabularios: MutableSet<VocabularioExterno> = mutableSetOf(),
    @Expose var sequencia: Int = 0
) : Manga() {

    fun addTexto(texto: MangaTexto) = textos.add(texto)

    val descricao: String get() = ((prefixo + " || " + String.format("%03f", capitulo)) + " - (" + String.format("%03d", numero)) + ") " + nomePagina

    constructor(id: UUID?, nomePagina: String, numero: Int, hash: String) : this() {
        setInitial("", 0, 0f, numero, nomePagina)
        this.id = id
        this.hash = hash
    }

    constructor(id: UUID?, nomePagina: String, numero: Int, hash: String, textos: MutableList<MangaTexto>) : this() {
        setInitial("", 0, 0f, numero, nomePagina)
        this.id = id
        this.hash = hash
        this.textos = textos
    }

    constructor(id: UUID?, nomePagina: String, numero: Int, hash: String, textos: MutableList<MangaTexto>, vocabularios: MutableSet<VocabularioExterno>) : this() {
        setInitial("", 0, 0f, numero, nomePagina)
        this.id = id
        this.hash = hash
        this.vocabularios = vocabularios
        this.textos = textos
    }

    override fun toString(): String {
        return ("MangaPagina [id=" + id + ", nomePagina=" + nomePagina + ", numero=" + numero + ", hash=" + hash
                + ", textos=" + textos + ", sequencia=" + sequencia + "]")
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), id, nomePagina, numero, hash)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaPagina

        if (id != other.id) return false
        if (nomePagina != other.nomePagina) return false
        if (numero != other.numero) return false
        if (hash != other.hash) return false

        return true
    }
}