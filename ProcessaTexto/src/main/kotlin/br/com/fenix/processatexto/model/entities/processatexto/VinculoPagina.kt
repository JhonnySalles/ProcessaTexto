package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Pagina
import javafx.scene.image.Image
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaPagina
import java.io.Serializable
import java.util.*

data class VinculoPagina(
    private var id: Long?,
    var originalHash: String = "",
    var originalNomePagina: String = "",
    var originalPathPagina: String = "",
    var originalPagina: Int = PAGINA_VAZIA,
    var originalPaginas: Int = 0,
    var isOriginalPaginaDupla: Boolean = false,
    var originalPHash: String = "",
    var originalHistogram: FloatArray? = null,
    var vinculadoDireitaHash: String = "",
    var vinculadoDireitaNomePagina: String = "",
    var vinculadoDireitaPathPagina: String = "",
    var vinculadoDireitaPagina: Int = PAGINA_VAZIA,
    var vinculadoDireitaPaginas: Int = 0,
    var isVinculadoDireitaPaginaDupla: Boolean = false,
    var vinculadoDireitaPHash: String = "",
    var vinculadoDireitaHistogram: FloatArray? = null,
    var vinculadoEsquerdaHash: String = "",
    var vinculadoEsquerdaNomePagina: String = "",
    var vinculadoEsquerdaPathPagina: String = "",
    var vinculadoEsquerdaPagina: Int = PAGINA_VAZIA,
    var vinculadoEsquerdaPaginas: Int = 0,
    var isVinculadoEsquerdaPaginaDupla: Boolean = false,
    var vinculadoEsquerdaPHash: String = "",
    var vinculadoEsquerdaHistogram: FloatArray? = null,
    @Transient
    var mangaPaginaOriginal: MangaPagina? = null,
    @Transient
    var mangaPaginaDireita: MangaPagina? = null,
    @Transient
    var mangaPaginaEsquerda: MangaPagina? = null,
    @Transient
    var imagemOriginal: Image? = null,
    @Transient
    var imagemVinculadoDireita: Image? = null,
    @Transient
    var imagemVinculadoEsquerda: Image? = null,
    var isImagemDupla: Boolean = false,
    var isNaoVinculado: Boolean = false,
    var onDragOrigem: Pagina? = null,
) :  EntityBase<Long?, VinculoPagina>(), Serializable {

    companion object {
        private const val serialVersionUID = 1595061547917015448L
        val PAGINA_VAZIA: Int = -1
    }

    fun mesclar(outro: VinculoPagina) {
        vinculadoEsquerdaNomePagina = outro.vinculadoEsquerdaNomePagina
        vinculadoEsquerdaPathPagina = outro.vinculadoEsquerdaPathPagina
        vinculadoEsquerdaPagina = outro.vinculadoEsquerdaPagina
        vinculadoEsquerdaPaginas = outro.vinculadoEsquerdaPaginas
        vinculadoEsquerdaHash = outro.vinculadoEsquerdaHash
        vinculadoEsquerdaPHash = outro.vinculadoEsquerdaPHash
        vinculadoEsquerdaHistogram = outro.vinculadoEsquerdaHistogram
        isVinculadoEsquerdaPaginaDupla = outro.isVinculadoEsquerdaPaginaDupla
        mangaPaginaEsquerda = outro.mangaPaginaEsquerda
        imagemVinculadoEsquerda = outro.imagemVinculadoEsquerda
        vinculadoDireitaNomePagina = outro.vinculadoDireitaNomePagina
        vinculadoDireitaPathPagina = outro.vinculadoDireitaPathPagina
        vinculadoDireitaPagina = outro.vinculadoDireitaPagina
        vinculadoDireitaPaginas = outro.vinculadoDireitaPaginas
        vinculadoDireitaHash = outro.vinculadoDireitaHash
        vinculadoDireitaPHash = outro.vinculadoDireitaPHash
        vinculadoDireitaHistogram = outro.vinculadoDireitaHistogram
        isVinculadoDireitaPaginaDupla = outro.isVinculadoDireitaPaginaDupla
        mangaPaginaDireita = outro.mangaPaginaDireita
        imagemVinculadoDireita = outro.imagemVinculadoDireita
        isImagemDupla = outro.isImagemDupla
    }

    fun addOriginalSemId(original: VinculoPagina) {
        id = null
        originalNomePagina = original.originalNomePagina
        originalPathPagina = original.originalPathPagina
        originalPagina = original.originalPagina
        originalPaginas = original.originalPaginas
        originalHash = original.originalHash
        originalPHash = original.originalPHash
        originalHistogram = original.originalHistogram
        isOriginalPaginaDupla = original.isOriginalPaginaDupla
        mangaPaginaOriginal = original.mangaPaginaOriginal
        imagemOriginal = original.imagemOriginal
    }

    fun addOriginal(original: VinculoPagina) {
        addOriginal(
            original.id, original.originalNomePagina, original.originalPathPagina, original.originalPagina,
            original.originalPaginas, original.isOriginalPaginaDupla, original.mangaPaginaOriginal,
            original.imagemOriginal, original.originalHash, original.originalPHash, original.originalHistogram
        )
    }

    fun addOriginal(
        id: Long?, originalNomePagina: String, originalPathPagina: String, originalPagina: Int,
        originalPaginas: Int, isOriginalPaginaDupla: Boolean, mangaPaginaOriginal: MangaPagina?,
        imagemOriginal: Image?, originalHash: String, originalPHash: String, originalHistogram: FloatArray?
    ) {
        this.id = id
        this.originalNomePagina = originalNomePagina
        this.originalPathPagina = originalPathPagina
        this.originalPagina = originalPagina
        this.originalPaginas = originalPaginas
        this.originalHash = originalHash
        this.originalPHash = originalPHash
        this.originalHistogram = originalHistogram
        this.isOriginalPaginaDupla = isOriginalPaginaDupla
        this.mangaPaginaOriginal = mangaPaginaOriginal
        this.imagemOriginal = imagemOriginal
    }

    fun addVinculoEsquerdaApartirDireita(vinculo: VinculoPagina) {
        addVinculoEsquerda(
            vinculo.vinculadoDireitaNomePagina, vinculo.vinculadoDireitaPathPagina,
            vinculo.vinculadoDireitaPagina, vinculo.vinculadoDireitaPaginas, vinculo.isVinculadoDireitaPaginaDupla,
            vinculo.mangaPaginaDireita, vinculo.imagemVinculadoDireita, vinculo.vinculadoDireitaHash,
            vinculo.vinculadoDireitaPHash, vinculo.vinculadoDireitaHistogram
        )
    }

    fun addVinculoEsquerda(vinculo: VinculoPagina) {
        addVinculoEsquerda(
            vinculo.vinculadoEsquerdaNomePagina, vinculo.vinculadoEsquerdaPathPagina,
            vinculo.vinculadoEsquerdaPagina, vinculo.vinculadoEsquerdaPaginas,
            vinculo.isVinculadoEsquerdaPaginaDupla, vinculo.mangaPaginaEsquerda, vinculo.imagemVinculadoEsquerda,
            vinculo.vinculadoEsquerdaHash, vinculo.vinculadoEsquerdaPHash, vinculo.vinculadoEsquerdaHistogram
        )
    }

    fun addVinculoEsquerda(
        vinculadoEsquerdaNomePagina: String, vinculadoEsquerdaPathPagina: String,
        vinculadoEsquerdaPagina: Int, vinculadoEsquerdaPaginas: Int, isVinculadoEsquerdaPaginaDupla: Boolean,
        mangaPaginaEsquerda: MangaPagina?, imagemVinculadoEsquerda: Image?, vinculadoEsquerdaHash: String,
        vinculadoEsquerdaPHash: String, vinculadoEsquerdaHistogram: FloatArray?
    ) {
        this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina
        this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina
        this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina
        this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas
        this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla
        this.mangaPaginaEsquerda = mangaPaginaEsquerda
        this.imagemVinculadoEsquerda = imagemVinculadoEsquerda
        this.vinculadoEsquerdaHash = vinculadoEsquerdaHash
        this.vinculadoEsquerdaPHash = vinculadoEsquerdaPHash
        this.vinculadoEsquerdaHistogram = vinculadoEsquerdaHistogram
    }

    fun addVinculoDireitaApartirEsquerda(vinculo: VinculoPagina) {
        addVinculoDireita(
            vinculo.vinculadoEsquerdaNomePagina, vinculo.vinculadoEsquerdaPathPagina,
            vinculo.vinculadoEsquerdaPagina, vinculo.vinculadoEsquerdaPaginas,
            vinculo.isVinculadoEsquerdaPaginaDupla, vinculo.mangaPaginaEsquerda, vinculo.imagemVinculadoEsquerda,
            vinculo.vinculadoEsquerdaHash, vinculo.vinculadoEsquerdaPHash, vinculo.vinculadoEsquerdaHistogram
        )
    }

    fun addVinculoDireita(vinculo: VinculoPagina) {
        addVinculoDireita(
            vinculo.vinculadoDireitaNomePagina, vinculo.vinculadoDireitaPathPagina,
            vinculo.vinculadoDireitaPagina, vinculo.vinculadoDireitaPaginas, vinculo.isVinculadoDireitaPaginaDupla,
            vinculo.mangaPaginaDireita, vinculo.imagemVinculadoDireita, vinculo.vinculadoDireitaHash,
            vinculo.vinculadoDireitaPHash, vinculo.vinculadoDireitaHistogram
        )
    }

    fun addVinculoDireita(
        vinculadoDireitaNomePagina: String, vinculadoDireitaPathPagina: String,
        vinculadoDireitaPagina: Int, vinculadoDireitaPaginas: Int, isVinculadoDireitaPaginaDupla: Boolean,
        mangaPaginaDireita: MangaPagina?, imagemVinculadoDireita: Image?, vinculadoDireitaHash: String,
        vinculadoDireitaPHash: String, vinculadoDireitaHistogram: FloatArray?
    ) {
        if (vinculadoEsquerdaPagina === PAGINA_VAZIA) {
            vinculadoEsquerdaNomePagina = vinculadoDireitaNomePagina
            vinculadoEsquerdaPathPagina = vinculadoDireitaPathPagina
            vinculadoEsquerdaPagina = vinculadoDireitaPagina
            vinculadoEsquerdaPaginas = vinculadoDireitaPaginas
            vinculadoEsquerdaHash = vinculadoDireitaHash
            vinculadoEsquerdaPHash = vinculadoDireitaPHash
            vinculadoEsquerdaHistogram = vinculadoDireitaHistogram
            isVinculadoEsquerdaPaginaDupla = isVinculadoDireitaPaginaDupla
            mangaPaginaEsquerda = mangaPaginaDireita
            imagemVinculadoEsquerda = imagemVinculadoDireita
            isImagemDupla = this.vinculadoDireitaPagina !== PAGINA_VAZIA
        } else {
            this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina
            this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina
            this.vinculadoDireitaPagina = vinculadoDireitaPagina
            this.vinculadoDireitaPaginas = vinculadoDireitaPaginas
            this.vinculadoDireitaHash = vinculadoDireitaHash
            this.vinculadoDireitaPHash = vinculadoDireitaPHash
            this.vinculadoDireitaHistogram = vinculadoDireitaHistogram
            this.isVinculadoDireitaPaginaDupla = isVinculadoDireitaPaginaDupla
            this.mangaPaginaDireita = mangaPaginaDireita
            this.imagemVinculadoDireita = imagemVinculadoDireita
            isImagemDupla = true
        }
    }

    fun moverDireitaParaEsquerda() {
        vinculadoEsquerdaNomePagina = vinculadoDireitaNomePagina
        vinculadoEsquerdaPathPagina = vinculadoDireitaPathPagina
        vinculadoEsquerdaPagina = vinculadoDireitaPagina
        vinculadoEsquerdaPaginas = vinculadoDireitaPaginas
        vinculadoEsquerdaHash = vinculadoDireitaHash
        vinculadoEsquerdaPHash = vinculadoDireitaPHash
        vinculadoEsquerdaHistogram = vinculadoDireitaHistogram
        mangaPaginaEsquerda = mangaPaginaDireita
        imagemVinculadoEsquerda = imagemVinculadoDireita
        isVinculadoEsquerdaPaginaDupla = isVinculadoDireitaPaginaDupla
        limparVinculadoDireita()
    }

    fun limparVinculado() {
        vinculadoDireitaNomePagina = ""
        vinculadoDireitaPathPagina = ""
        vinculadoDireitaPagina = PAGINA_VAZIA
        vinculadoDireitaPaginas = 0
        vinculadoDireitaHash = ""
        vinculadoDireitaPHash = ""
        vinculadoDireitaHistogram = null
        vinculadoEsquerdaNomePagina = ""
        vinculadoEsquerdaPathPagina = ""
        vinculadoEsquerdaPagina = PAGINA_VAZIA
        vinculadoEsquerdaPaginas = 0
        vinculadoEsquerdaHash = ""
        vinculadoEsquerdaPHash = ""
        vinculadoEsquerdaHistogram = null
        mangaPaginaDireita = null
        mangaPaginaEsquerda = null
        imagemVinculadoDireita = null
        imagemVinculadoEsquerda = null
        isVinculadoDireitaPaginaDupla = false
        isVinculadoEsquerdaPaginaDupla = false
        isImagemDupla = false
        isNaoVinculado = false
    }

    fun limparVinculadoEsquerda(): Boolean {
        return limparVinculadoEsquerda(false)
    }

    fun limparVinculadoEsquerda(isMover: Boolean): Boolean {
        var movido = false
        if (isMover && vinculadoDireitaPagina !== PAGINA_VAZIA) {
            vinculadoEsquerdaNomePagina = vinculadoDireitaNomePagina
            vinculadoEsquerdaPathPagina = vinculadoDireitaPathPagina
            vinculadoEsquerdaPagina = vinculadoDireitaPagina
            vinculadoEsquerdaPaginas = vinculadoDireitaPaginas
            vinculadoEsquerdaHash = vinculadoDireitaHash
            vinculadoEsquerdaPHash = vinculadoDireitaPHash
            vinculadoEsquerdaHistogram = vinculadoDireitaHistogram
            mangaPaginaEsquerda = mangaPaginaDireita
            imagemVinculadoEsquerda = imagemVinculadoDireita
            limparVinculadoDireita()
            movido = true
        } else {
            vinculadoEsquerdaNomePagina = ""
            vinculadoEsquerdaPathPagina = ""
            vinculadoEsquerdaPagina = PAGINA_VAZIA
            vinculadoEsquerdaPaginas = 0
            vinculadoEsquerdaHash = ""
            vinculadoEsquerdaPHash = ""
            vinculadoEsquerdaHistogram = null
            isVinculadoEsquerdaPaginaDupla = false
            imagemVinculadoEsquerda = null
            mangaPaginaEsquerda = null
            isImagemDupla = false
        }
        return movido
    }

    fun limparVinculadoDireita() {
        vinculadoDireitaNomePagina = ""
        vinculadoDireitaPathPagina = ""
        vinculadoDireitaPagina = PAGINA_VAZIA
        vinculadoDireitaPaginas = 0
        vinculadoDireitaHash = ""
        vinculadoDireitaPHash = ""
        vinculadoDireitaHistogram = null
        isVinculadoDireitaPaginaDupla = false
        isImagemDupla = false
        imagemVinculadoDireita = null
        mangaPaginaDireita = null
    }

    constructor(
        originalNomePagina: String, originalPathPagina: String, originalPagina: Int,
        originalPaginas: Int, isOriginalPaginaDupla: Boolean, mangaPaginaOriginal: MangaPagina?,
        imagemOriginal: Image?, originalHash: String, originalPHash: String, originalHistogram: FloatArray
    ) : this(null) {
        this.originalNomePagina = originalNomePagina
        this.originalPathPagina = originalPathPagina
        this.originalPagina = originalPagina
        this.originalPaginas = originalPaginas
        this.originalHash = originalHash
        this.originalPHash = originalPHash
        this.originalHistogram = originalHistogram
        this.mangaPaginaOriginal = mangaPaginaOriginal
        this.imagemOriginal = imagemOriginal
        this.isOriginalPaginaDupla = isOriginalPaginaDupla
    }

    constructor(
        vinculadoEsquerdaNomePagina: String, vinculadoEsquerdaPathPagina: String,
        vinculadoEsquerdaPagina: Int, vinculadoEsquerdaPaginas: Int, isVinculadoEsquerdaPaginaDupla: Boolean,
        mangaPaginaEsquerda: MangaPagina?, imagemVinculadoEsquerda: Image?, naoVinculado: Boolean,
        vinculadoEsquerdaHash: String, vinculadoEsquerdaPHash: String, vinculadoEsquerdaHistogram: FloatArray
    ) : this(null) {
        this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina
        this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina
        this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina
        this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas
        this.vinculadoEsquerdaHash = vinculadoEsquerdaHash
        this.vinculadoEsquerdaPHash = vinculadoEsquerdaPHash
        this.vinculadoEsquerdaHistogram = vinculadoEsquerdaHistogram
        this.mangaPaginaEsquerda = mangaPaginaEsquerda
        this.imagemVinculadoEsquerda = imagemVinculadoEsquerda
        this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla
        isNaoVinculado = naoVinculado
    }

    constructor(
        id: Long, vinculadoEsquerdaNomePagina: String, vinculadoEsquerdaPathPagina: String,
        vinculadoEsquerdaPagina: Int, vinculadoEsquerdaPaginas: Int, isVinculadoEsquerdaPaginaDupla: Boolean,
        mangaPaginaEsquerda: MangaPagina?, naoVinculado: Boolean
    ) : this(id) {
        this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina
        this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina
        this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina
        this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas
        this.mangaPaginaEsquerda = mangaPaginaEsquerda
        this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla
        isNaoVinculado = naoVinculado
    }

    constructor(
        id: Long, originalNomePagina: String, originalPathPagina: String, originalPagina: Int,
        originalPaginas: Int, isOriginalPaginaDupla: Boolean, vinculadoDireitaNomePagina: String,
        vinculadoDireitaPathPagina: String, vinculadoDireitaPagina: Int, vinculadoDireitaPaginas: Int,
        isVinculadoDireitaPaginaDupla: Boolean, vinculadoEsquerdaNomePagina: String,
        vinculadoEsquerdaPathPagina: String, vinculadoEsquerdaPagina: Int, vinculadoEsquerdaPaginas: Int,
        isVinculadoEsquerdaPaginaDupla: Boolean, mangaPaginaOriginal: MangaPagina?, mangaPaginaDireita: MangaPagina?,
        mangaPaginaEsquerda: MangaPagina?, imagemDupla: Boolean, naoVinculado: Boolean
    ) : this(id) {
        this.originalNomePagina = originalNomePagina
        this.originalPathPagina = originalPathPagina
        this.originalPagina = originalPagina
        this.originalPaginas = originalPaginas
        this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina
        this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina
        this.vinculadoDireitaPagina = vinculadoDireitaPagina
        this.vinculadoDireitaPaginas = vinculadoDireitaPaginas
        this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina
        this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina
        this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina
        this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas
        this.mangaPaginaOriginal = mangaPaginaOriginal
        this.mangaPaginaDireita = mangaPaginaDireita
        this.mangaPaginaEsquerda = mangaPaginaEsquerda
        this.isOriginalPaginaDupla = isOriginalPaginaDupla
        this.isVinculadoDireitaPaginaDupla = isVinculadoDireitaPaginaDupla
        this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla
        isImagemDupla = imagemDupla
        isNaoVinculado = naoVinculado
    }

    constructor(
        id: Long, originalNomePagina: String, originalPathPagina: String, originalPagina: Int,
        originalPaginas: Int, isOriginalPaginaDupla: Boolean, vinculadoDireitaNomePagina: String,
        vinculadoDireitaPathPagina: String, vinculadoDireitaPagina: Int, vinculadoDireitaPaginas: Int,
        isVinculadoDireitaPaginaDupla: Boolean, vinculadoEsquerdaNomePagina: String,
        vinculadoEsquerdaPathPagina: String, vinculadoEsquerdaPagina: Int, vinculadoEsquerdaPaginas: Int,
        isVinculadoEsquerdaPaginaDupla: Boolean, mangaPaginaOriginal: MangaPagina, mangaPaginaDireita: MangaPagina?,
        mangaPaginaEsquerda: MangaPagina?, imagemOriginal: Image, imagemVinculadoDireita: Image,
        imagemVinculadoEsquerda: Image, imagemDupla: Boolean, naoVinculado: Boolean, originalHash: String,
        vinculadoDireitaHash: String, vinculadoEsquerdaHash: String
    ) : this(id) {
        this.id = id
        this.originalNomePagina = originalNomePagina
        this.originalPathPagina = originalPathPagina
        this.originalPagina = originalPagina
        this.originalPaginas = originalPaginas
        this.originalHash = originalHash
        this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina
        this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina
        this.vinculadoDireitaPagina = vinculadoDireitaPagina
        this.vinculadoDireitaPaginas = vinculadoDireitaPaginas
        this.vinculadoDireitaHash = vinculadoDireitaHash
        this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina
        this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina
        this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina
        this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas
        this.vinculadoEsquerdaHash = vinculadoEsquerdaHash
        this.mangaPaginaOriginal = mangaPaginaOriginal
        this.mangaPaginaDireita = mangaPaginaDireita
        this.mangaPaginaEsquerda = mangaPaginaEsquerda
        this.imagemOriginal = imagemOriginal
        this.imagemVinculadoDireita = imagemVinculadoDireita
        this.imagemVinculadoEsquerda = imagemVinculadoEsquerda
        this.isOriginalPaginaDupla = isOriginalPaginaDupla
        this.isVinculadoDireitaPaginaDupla = isVinculadoDireitaPaginaDupla
        this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla
        isImagemDupla = imagemDupla
        isNaoVinculado = naoVinculado
    }

    constructor(manga: VinculoPagina) : this(null) {
        originalNomePagina = manga.originalNomePagina
        originalPathPagina = manga.originalPathPagina
        originalPagina = manga.originalPagina
        originalPaginas = manga.originalPaginas
        originalHash = manga.originalHash
        originalPHash = manga.originalPHash
        originalHistogram = manga.originalHistogram
        mangaPaginaOriginal = manga.mangaPaginaOriginal
        imagemOriginal = manga.imagemOriginal
        isOriginalPaginaDupla = manga.isOriginalPaginaDupla
    }

    constructor(manga: VinculoPagina, isEsquerda: Boolean, isNaoVinculado: Boolean) : this(null) {
        if (isEsquerda) {
            vinculadoEsquerdaNomePagina = manga.vinculadoEsquerdaNomePagina
            vinculadoEsquerdaPathPagina = manga.vinculadoEsquerdaPathPagina
            vinculadoEsquerdaPagina = manga.vinculadoEsquerdaPagina
            vinculadoEsquerdaPaginas = manga.vinculadoEsquerdaPaginas
            vinculadoEsquerdaHash = manga.vinculadoEsquerdaHash
            vinculadoEsquerdaPHash = manga.vinculadoEsquerdaPHash
            mangaPaginaEsquerda = manga.mangaPaginaEsquerda
            imagemVinculadoEsquerda = manga.imagemVinculadoEsquerda
            isVinculadoEsquerdaPaginaDupla = manga.isVinculadoEsquerdaPaginaDupla
            vinculadoEsquerdaHistogram = manga.vinculadoEsquerdaHistogram
        } else {
            vinculadoEsquerdaNomePagina = manga.vinculadoDireitaNomePagina
            vinculadoEsquerdaPathPagina = manga.vinculadoDireitaPathPagina
            vinculadoEsquerdaPagina = manga.vinculadoDireitaPagina
            vinculadoEsquerdaPaginas = manga.vinculadoDireitaPaginas
            vinculadoEsquerdaHash = manga.vinculadoDireitaHash
            vinculadoEsquerdaPHash = manga.vinculadoDireitaPHash
            mangaPaginaEsquerda = manga.mangaPaginaDireita
            imagemVinculadoEsquerda = manga.imagemVinculadoDireita
            isVinculadoEsquerdaPaginaDupla = manga.isVinculadoDireitaPaginaDupla
            vinculadoEsquerdaHistogram = manga.vinculadoDireitaHistogram
        }
        this.isNaoVinculado = isNaoVinculado
    }

    @Override
    override fun toString(): String {
        return ("VinculoPagina [id=" + id + ", originalNomePagina=" + originalNomePagina + ", originalPathPagina="
                + originalPathPagina + ", originalPagina=" + originalPagina + ", originalPaginas=" + originalPaginas
                + ", isOriginalPaginaDupla=" + isOriginalPaginaDupla + ", vinculadoDireitaNomePagina="
                + vinculadoDireitaNomePagina + ", vinculadoDireitaPathPagina=" + vinculadoDireitaPathPagina
                + ", vinculadoDireitaPagina=" + vinculadoDireitaPagina + ", vinculadoDireitaPaginas="
                + vinculadoDireitaPaginas + ", isVinculadoDireitaPaginaDupla=" + isVinculadoDireitaPaginaDupla
                + ", vinculadoEsquerdaNomePagina=" + vinculadoEsquerdaNomePagina + ", vinculadoEsquerdaPathPagina="
                + vinculadoEsquerdaPathPagina + ", vinculadoEsquerdaPagina=" + vinculadoEsquerdaPagina
                + ", vinculadoEsquerdaPaginas=" + vinculadoEsquerdaPaginas + ", isVinculadoEsquerdaPaginaDupla="
                + isVinculadoEsquerdaPaginaDupla + ", imagemDupla=" + isImagemDupla + ", naoVinculado=" + isNaoVinculado
                + "]")
    }

    @Override
    override fun hashCode(): Int {
        return Objects.hash(
            originalNomePagina, originalPagina, originalPaginas, originalPathPagina,
            vinculadoDireitaNomePagina, vinculadoDireitaPagina, vinculadoDireitaPaginas, vinculadoDireitaPathPagina,
            vinculadoEsquerdaNomePagina, vinculadoEsquerdaPagina, vinculadoEsquerdaPaginas,
            vinculadoEsquerdaPathPagina
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VinculoPagina

        if (originalNomePagina != other.originalNomePagina) return false
        if (originalPathPagina != other.originalPathPagina) return false
        if (originalPagina != other.originalPagina) return false
        if (originalPaginas != other.originalPaginas) return false
        if (vinculadoDireitaNomePagina != other.vinculadoDireitaNomePagina) return false
        if (vinculadoDireitaPathPagina != other.vinculadoDireitaPathPagina) return false
        if (vinculadoDireitaPagina != other.vinculadoDireitaPagina) return false
        if (vinculadoDireitaPaginas != other.vinculadoDireitaPaginas) return false
        if (vinculadoEsquerdaNomePagina != other.vinculadoEsquerdaNomePagina) return false
        if (vinculadoEsquerdaPathPagina != other.vinculadoEsquerdaPathPagina) return false
        if (vinculadoEsquerdaPagina != other.vinculadoEsquerdaPagina) return false
        if (vinculadoEsquerdaPaginas != other.vinculadoEsquerdaPaginas) return false

        return true
    }


    override fun getId(): Long? = id

    fun setId(id: Long?) {
        this.id = id
    }

    override fun create(id: Long?): VinculoPagina = VinculoPagina(id)
}