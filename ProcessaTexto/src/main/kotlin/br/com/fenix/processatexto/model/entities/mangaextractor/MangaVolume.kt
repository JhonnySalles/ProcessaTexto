package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Entity
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import com.google.gson.annotations.Expose
import java.util.*


data class MangaVolume(
    private var id: UUID? = null,
    @Expose override var manga: String = "",
    @Expose override var volume: Int = 0,
    @Expose var lingua: Language = Language.PORTUGUESE,
    @Expose var capitulos: MutableList<MangaCapitulo> = mutableListOf(),
    @Expose var vocabularios: MutableSet<VocabularioExterno> = mutableSetOf(),
    @Expose var arquivo: String = "",
    var processado: Boolean = false,
    var capa: MangaCapa? = null
) : Manga(), Entity<UUID?, MangaVolume> {

    init {
        super.isProcessar = true
    }

    override fun getId(): UUID? = id

    fun setId(id: UUID?) {
        this.id = id
    }

    override fun create(id: UUID?): MangaVolume {
        TODO("Not yet implemented")
    }

    fun addCapitulos(capitulo: MangaCapitulo) = capitulos.add(capitulo)

    fun addVocabulario(vocabulario: VocabularioExterno) = vocabularios.add(vocabulario)

    constructor(id: UUID?, manga: String, volume: Int, lingua: Language, arquivo: String) : this() {
        setInitial(manga, volume, 0f)
        this.id = id
        this.lingua = lingua
        this.arquivo = arquivo
    }

    constructor(id: UUID?, manga: String, volume: Int, lingua: Language, arquivo: String, capitulos: MutableList<MangaCapitulo>) : this() {
        setInitial(manga, volume, null)
        this.id = id
        this.manga = manga
        this.volume = volume
        this.lingua = lingua
        this.arquivo = arquivo
        this.capitulos = capitulos
    }

    constructor(id: UUID?, manga: String, volume: Int, lingua: Language, arquivo: String, vocabularios: MutableSet<VocabularioExterno>) : this() {
        setInitial(manga, volume, 0f)
        this.id = id
        this.lingua = lingua
        this.vocabularios = vocabularios
        this.arquivo = arquivo
    }

    constructor(
        id: UUID?,
        manga: String,
        volume: Int,
        lingua: Language,
        arquivo: String,
        vocabularios: MutableSet<VocabularioExterno>,
        capitulos: MutableList<MangaCapitulo>,
        capa: MangaCapa?
    ) : this() {
        setInitial(manga, volume, 0f)
        this.id = id
        this.lingua = lingua
        this.capitulos = capitulos
        this.vocabularios = vocabularios
        this.arquivo = arquivo
        this.capa = capa
    }

    override fun toString(): String {
        return "MangaVolume [id=$id, manga=$manga, volume=$volume, lingua=$lingua, vocabularios=$vocabularios, arquivo=$arquivo]"
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), id, manga, volume, lingua)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaVolume

        if (id != other.id) return false
        if (manga != other.manga) return false
        if (volume != other.volume) return false
        if (lingua != other.lingua) return false

        return true
    }
}