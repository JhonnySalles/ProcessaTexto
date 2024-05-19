package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Entity
import br.com.fenix.processatexto.model.entities.Manga
import com.google.gson.annotations.Expose
import java.util.*


data class MangaTexto(
    private var id: UUID? = null,
    @Expose override var texto: String = "",
    @Expose var sequencia: Int = 0,
    @Expose var x1: Int = 0,
    @Expose var y1: Int = 0,
    @Expose var x2: Int = 0,
    @Expose var y2: Int = 0
) : Manga(), Entity<UUID?, MangaTexto> {

    override fun getId(): UUID? = id

    fun setId(id: UUID?) {
        this.id = id
    }

    override fun create(id: UUID?): MangaTexto {
        TODO("Not yet implemented")
    }

    @Override
    override fun toString(): String {
        return "MangaTexto [id=$id, texto=$texto, sequencia=$sequencia]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaTexto

        if (id != other.id) return false
        if (texto != other.texto) return false
        if (sequencia != other.sequencia) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + texto.hashCode()
        result = 31 * result + sequencia
        return result
    }

}