package br.com.fenix.processatexto.model.entities.processatexto

import com.google.gson.annotations.Expose
import java.util.*


data class VocabularioExterno(
    @Expose private var _palavra: String = "",
    @Expose var revisado: Boolean = false
) : Vocabulario() {

    /*fun setPalavra(palavra: String) {
        this.palavra = palavra
        this.vocabulario = palavra
    }*/

    var palavra : String
    get() = _palavra
    set(palavra) {
        this._palavra = palavra
        this.vocabulario = palavra
    }

    constructor(palavra: String, portugues: String, ingles: String, leitura: String) : this() {
        this.vocabulario = palavra
        this.palavra = palavra
        this.leitura = leitura
        this.ingles = ingles
        this.portugues = portugues
        revisado = true
    }

    constructor(id: UUID?, palavra: String, portugues: String, revisado: Boolean) : this() {
        setId(id)
        this.vocabulario = palavra
        this.portugues = portugues
        this.palavra = palavra
        this.revisado = revisado
    }

    constructor(id: UUID?, palavra: String, portugues: String, ingles: String, leitura: String, revisado: Boolean) : this() {
        setId(id)
        this.vocabulario = palavra
        this.leitura = leitura
        this.ingles = ingles
        this.portugues = portugues
        this.palavra = palavra
        this.revisado = revisado
    }

    //Japones
    constructor(id: UUID?, palavra: String, portugues: String, ingles: String, leitura: String, leituraNovel: String, revisado: Boolean) : this() {
        setId(id)
        this.vocabulario = palavra
        this.leitura = leitura
        this.leituraNovel = leituraNovel
        this.ingles = ingles
        this.portugues = portugues
        this.palavra = palavra
        this.revisado = revisado
    }

}