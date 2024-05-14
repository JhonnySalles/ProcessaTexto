package br.com.fenix.processatexto.model.entities.comicinfo

data class MAL(
    var arquivo: String = "",
    var myanimelist: MutableList<Registro> = mutableListOf()
) : BaseLista("", "", 0) {


    inner class Registro(val parent: MAL, override var nome: String, override var id: Long, processar: Boolean) : BaseLista("", nome, id) {
        init {
            isMarcado = processar
        }

        /*fun setId(id: Long) {
            this.id = id
            this.idVisual = id.toString()
        }*/
    }

    fun addRegistro(nome: String, id: Long, processar: Boolean): Registro {
        val item = Registro(this, nome, id, processar)
        item.isMarcado = processar
        myanimelist.add(item)
        return item
    }

    constructor(arquivo: String, nome: String, myanimelist: MutableList<Registro>) : this() {
        this.arquivo = arquivo
        this.myanimelist = myanimelist
        this.nome = nome
        this.isMarcado = false
    }

    constructor(arquivo: String, nome: String) : this() {
        this.arquivo = arquivo
        this.nome = nome
        myanimelist = mutableListOf()
        this.isMarcado = false
    }
}