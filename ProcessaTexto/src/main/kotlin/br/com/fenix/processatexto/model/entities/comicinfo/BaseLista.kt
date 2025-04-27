package br.com.fenix.processatexto.model.entities.comicinfo

import com.jfoenix.controls.JFXButton
import javafx.scene.image.ImageView


open class BaseLista(
    var descricao: String,
    open var nome: String,
    private var _id: Long,
    var idVisual: String = "",
    var processar: JFXButton? = null,
    var myanimelist: JFXButton? = null,
    var amazon: JFXButton? = null,
    var imagem: ImageView? = null,
    var isSelecionado: Boolean = false,
    var isMarcado: Boolean = false
) {
    open var id: Long
        get() = _id
        set(value) {
            _id = value
            idVisual = if (_id > 0) _id.toString() else ""
        }

    constructor(descricao: String, nome: String, id: Long, processar: Boolean) :
    this(descricao, nome, id) {
        idVisual = if (id > 0) id.toString() else ""
        this.isMarcado = processar
    }

    fun setMyAnimeListButton(processar: JFXButton, myanimelist: JFXButton) {
        this.processar = processar
        this.myanimelist = myanimelist
    }

    fun setAmazonButton(amazon: JFXButton) {
        this.amazon = amazon
    }
}