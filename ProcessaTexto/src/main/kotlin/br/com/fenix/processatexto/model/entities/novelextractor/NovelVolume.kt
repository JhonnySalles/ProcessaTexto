package org.jisho.textosJapones.model.entities.novelextractor

import br.com.fenix.processatexto.model.entities.Novel
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapitulo
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import com.google.gson.annotations.Expose
import java.util.*

data class NovelVolume(
    var id: UUID? = null,
    @Expose override var novel: String = "",
    @Expose var titulo: String = "",
    @Expose var tituloAlternativo: String = "",
    @Expose var serie: String = "",
    @Expose var descricao: String = "",
    @Expose var arquivo: String = "",
    @Expose var editora: String = "",
    @Expose var autor: String = "",
    @Expose override var volume: Float = 0f,
    @Expose var lingua: Language = Language.PORTUGUESE,
    var capa: NovelCapa? = null,
    var isFavorito: Boolean = false,
    @Expose var capitulos: MutableList<NovelCapitulo> = mutableListOf(),
    @Expose var vocabularios: MutableSet<VocabularioExterno> = mutableSetOf(),
    var processado: Boolean = false
) : Novel() {


    constructor(
        id: UUID?,
        novel: String,
        titulo: String,
        tituloAlternativo: String,
        serie: String,
        descricao: String,
        arquivo: String,
        editora: String,
        autor: String,
        volume: Float,
        lingua: Language,
        favorito: Boolean,
        processado: Boolean
    ) : this() {
        this.id = id
        this.novel = novel
        this.titulo = titulo
        this.tituloAlternativo = tituloAlternativo
        this.serie = serie
        this.descricao = descricao
        this.arquivo = arquivo
        this.editora = editora
        this.autor = autor
        this.volume = volume
        this.lingua = lingua
        this.processado = processado
        capitulo = 0f
        isFavorito = favorito
        capa = null
        vocabularios = mutableSetOf()
        capitulos = mutableListOf()
    }

    constructor(
        id: UUID?,
        novel: String,
        titulo: String,
        tituloAlternativo: String,
        serie: String,
        descricao: String,
        arquivo: String,
        editora: String,
        autor: String,
        volume: Float,
        lingua: Language,
        favorito: Boolean,
        capa: NovelCapa?,
        processado: Boolean,
        capitulos: MutableList<NovelCapitulo>,
        vocabularios: MutableSet<VocabularioExterno>
    ) : this() {
        this.id = id
        this.novel = novel
        this.titulo = titulo
        this.tituloAlternativo = tituloAlternativo
        this.serie = serie
        this.descricao = descricao
        this.arquivo = arquivo
        this.editora = editora
        this.autor = autor
        this.volume = volume
        this.lingua = lingua
        isFavorito = favorito
        this.capa = capa
        this.capitulos = capitulos
        this.vocabularios = vocabularios
        this.processado = processado
        capitulo = 0f
    }

    fun addCapitulos(capitulo: NovelCapitulo) = capitulos.add(capitulo)

    override fun toString(): String {
        return "NovelVolume [" + "id=" + id + ", novel=" + novel + ", titulo=" + titulo + ", tituloAlternativo=" + tituloAlternativo + ", descricao=" + descricao +
                ", arquivo=" + arquivo + ", editora=" + editora + ", volume=" + volume + ", lingua=" + lingua + "]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as NovelVolume

        if (id != other.id) return false
        if (novel != other.novel) return false
        if (volume != other.volume) return false
        if (lingua != other.lingua) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + novel.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + lingua.hashCode()
        return result
    }
}