package br.com.fenix.processatexto.model.entities.novelextractor

import br.com.fenix.processatexto.model.entities.Novel
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import com.google.gson.annotations.Expose
import java.util.*


data class NovelCapitulo(
    var id: UUID? = null,
    @Expose override var novel: String = "",
    @Expose override var volume: Float = 0f,
    @Expose override var capitulo: Float = 0f,
    @Expose var descricao: String = "",
    @Expose var sequencia: Int = 0,
    @Expose var lingua: Language = Language.PORTUGUESE,
    @Expose var textos: MutableList<NovelTexto> = mutableListOf(),
    @Expose var vocabularios: MutableSet<VocabularioExterno> = mutableSetOf()
) : Novel() {

    constructor(id: UUID?, novel: String, volume: Float, capitulo: Float, descricao: String, sequencia: Int, lingua: Language) : this() {
        this.id = id
        this.novel = novel
        this.volume = volume
        this.capitulo = capitulo
        this.descricao = descricao
        this.sequencia = sequencia
        this.lingua = lingua
        vocabularios = mutableSetOf()
        textos = mutableListOf()
    }

    fun addVocabulario(vocabulario: VocabularioExterno) = vocabularios.add(vocabulario)

    fun addTexto(texto: NovelTexto) = textos.add(texto)

    override fun toString(): String {
        return "NovelCapitulo [id=$id, capitulo=$capitulo, lingua=$lingua, textos=$textos]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as NovelCapitulo

        if (id != other.id) return false
        if (novel != other.novel) return false
        if (volume != other.volume) return false
        if (capitulo != other.capitulo) return false
        if (lingua != other.lingua) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + novel.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + capitulo.hashCode()
        result = 31 * result + lingua.hashCode()
        return result
    }
}