package br.com.fenix.processatexto.model.entities.comicinfo


data class MAL(
    var arquivo: String = "",
    var comicInfo: ComicInfo? = null,
    var myAnimeList: MutableList<Registro> = mutableListOf()
) : BaseLista(arquivo, "", 0) {


    inner class Registro(val parent: MAL) : BaseLista("", "", 0)

    fun addRegistro(nome: String, id: Long, processar: Boolean): Registro {
        val item = Registro(this)
        item.id = id
        item.nome = nome
        item.isMarcado = processar
        myAnimeList.add(item)
        return item
    }

    constructor(arquivo: String, nome: String, comicInfo: ComicInfo?) : this(arquivo, comicInfo) {
        this.descricao = arquivo
        this.nome = nome
        myAnimeList = mutableListOf()
        this.isMarcado = false
    }
}