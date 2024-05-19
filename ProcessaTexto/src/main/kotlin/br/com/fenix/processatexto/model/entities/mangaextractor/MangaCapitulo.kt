package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Entity
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import com.google.gson.annotations.Expose
import java.util.*


data class MangaCapitulo(
    private var id: UUID? = null,
    @Expose override var manga: String = "",
    @Expose override var volume: Int = 0,
    @Expose override var capitulo: Float = 0f,
    @Expose var lingua: Language = Language.PORTUGUESE,
    @Expose var scan: String = "",
    @Expose var paginas: MutableList<MangaPagina> = mutableListOf(),
    @Expose var isExtra: Boolean = false,
    @Expose var isRaw: Boolean = false,
    @Expose var vocabularios: MutableSet<VocabularioExterno> = mutableSetOf()
) : Manga(), Entity<UUID?, MangaCapitulo> {

    override fun getId(): UUID? = id

    fun setId(id: UUID?) {
        this.id = id
    }

    override fun create(id: UUID?): MangaCapitulo {
        TODO("Not yet implemented")
    }

    fun addPaginas(pagina: MangaPagina) = paginas.add(pagina)

    fun addVocabulario(vocabulario: VocabularioExterno) = vocabularios.add(vocabulario)

    /*constructor() : super() {
        this.processar = true
    }*/

    constructor(id: UUID?, manga: String, volume: Int, capitulo: Float, lingua: Language, scan: String, extra: Boolean, raw: Boolean) :
            this() {
        setInitial(manga, volume, capitulo)
        this.id = id
        this.lingua = lingua
        this.scan = scan
        super.isProcessar = true
        isExtra = extra
        isRaw = raw
    }

    constructor(
        id: UUID?, manga: String, volume: Int, capitulo: Float, lingua: Language, scan: String,
        extra: Boolean, raw: Boolean, paginas: MutableList<MangaPagina>
    ) : this() {
        setInitial(manga, volume, capitulo)
        this.id = id
        this.lingua = lingua
        this.scan = scan
        super.isProcessar = true
        this.paginas = paginas
        isExtra = extra
        isRaw = raw
    }

    constructor(
        id: UUID?, manga: String, volume: Int, capitulo: Float, lingua: Language, scan: String,
        vocabularios: MutableSet<VocabularioExterno>
    ) : this() {
        setInitial(manga, volume, capitulo)
        this.id = id
        this.lingua = lingua
        this.scan = scan
        this.vocabularios = vocabularios
        super.isProcessar = true
        paginas = ArrayList<MangaPagina>()
    }

    constructor(
        id: UUID?, manga: String, volume: Int, capitulo: Float, lingua: Language, scan: String,
        extra: Boolean, raw: Boolean, vocabularios: MutableSet<VocabularioExterno>, paginas: MutableList<MangaPagina>
    ) : this() {
        setInitial(manga, volume, capitulo)
        this.id = id
        this.lingua = lingua
        this.scan = scan
        this.paginas = paginas
        this.vocabularios = vocabularios
        super.isProcessar = true
        isExtra = extra
        isRaw = raw
    }

    override fun toString(): String {
        return ("MangaCapitulo [id=" + id + ", capitulo=" + capitulo + ", lingua=" + lingua + ", scan=" + scan
                + ", paginas=" + paginas + ", extra=" + isExtra + ", raw=" + isRaw + "]")
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), id, manga, volume, capitulo, lingua, scan, isExtra, isRaw)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaCapitulo

        if (id != other.id) return false
        if (manga != other.manga) return false
        if (volume != other.volume) return false
        if (capitulo != other.capitulo) return false
        if (lingua != other.lingua) return false
        if (scan != other.scan) return false
        if (isExtra != other.isExtra) return false
        if (isRaw != other.isRaw) return false

        return true
    }
}