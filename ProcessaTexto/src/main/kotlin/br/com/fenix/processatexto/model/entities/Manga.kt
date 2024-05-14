package br.com.fenix.processatexto.model.entities

import br.com.fenix.processatexto.model.enums.Language
import java.util.*


open class Manga(
    open var base: String = "",
    open var manga: String = "",
    open var volume: Int = 0,
    open var capitulo: Float = 0F,
    open var pagina: Int = 0,
    open var nomePagina: String = "",
    open var texto: String = "",
    open var isProcessar: Boolean = false,
    open var linguagem: String = "",
    open var volumeDestino: Int? = null,
    open var capituloDestino: Float? = null,
    open var isAlterado: Boolean = false,
    open var isItemExcluido: Boolean = false,
    open var prefixo: String = "",
    open var origem: String = "",
    open var isVinculo: Boolean = false
) {

    constructor(base: String, manga: String, linguagem: String) :
            this(base, manga) {
        this.linguagem = linguagem
    }

    constructor(base: String, manga: String, linguagem: String, isVinculo: Boolean) :
            this(base, manga) {
        this.isVinculo = isVinculo
        this.linguagem = linguagem
    }

    constructor(base: String, manga: String, volume: Int, capitulo: Float, pagina: Int, nomePagina: String) :
            this(base, manga) {
        this.volume = volume
        this.capitulo = capitulo
        this.pagina = pagina
        this.nomePagina = nomePagina
        volumeDestino = volume
        capituloDestino = capitulo
    }

    fun setInitial(manga: String, volume: Int, capitulo: Float, pagina: Int, nomePagina: String) {
        this.manga = manga
        this.volume = volume
        this.capitulo = capitulo
        this.pagina = pagina
        this.nomePagina = nomePagina
        volumeDestino = volume
        capituloDestino = capitulo
    }

    fun setInitial(manga: String, volume: Int, capitulo: Float?) {
        this.manga = manga
        this.volume = volume
        volumeDestino = volume
        capituloDestino = capitulo
    }

    fun addOutrasInformacoes(origem: String, prefixo: String, capitulo: Float) {
        this.prefixo = prefixo
        this.origem = origem
        this.capitulo = capitulo
    }

    fun addOutrasInformacoes(base: String, manga: String, volume: Int, capitulo: Float, lingua: Language) {
        this.base = base
        this.manga = manga
        this.volume = volume
        this.capitulo = capitulo
        pagina = 0
        nomePagina = ""
        texto = ""
        isProcessar = true
        linguagem = lingua.sigla.uppercase(Locale.getDefault())
        capituloDestino = capitulo
        volumeDestino = volume
        isAlterado = false
        isItemExcluido = false
        prefixo = ""
        origem = ""
    }

    fun addOutrasInformacoes(
        base: String, manga: String, volume: Int, capitulo: Float, lingua: Language,
        pagina: Int, nomePagina: String, texto: String
    ) {
        this.base = base
        this.manga = manga
        this.volume = volume
        this.capitulo = capitulo
        this.pagina = pagina
        this.nomePagina = nomePagina
        this.texto = texto
        isProcessar = true
        linguagem = lingua.sigla.uppercase(Locale.getDefault())
        capituloDestino = capitulo
        volumeDestino = volume
        isAlterado = false
        isItemExcluido = false
        prefixo = ""
        origem = ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Manga

        if (base != other.base) return false
        if (manga != other.manga) return false
        if (linguagem != other.linguagem) return false

        return true
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + manga.hashCode()
        result = 31 * result + linguagem.hashCode()
        return result
    }

}