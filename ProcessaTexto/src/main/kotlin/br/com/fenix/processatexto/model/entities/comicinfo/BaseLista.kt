package br.com.fenix.processatexto.model.entities.comicinfo

import com.jfoenix.controls.JFXButton
import javafx.scene.image.ImageView


open class BaseLista(
    var descricao: String,
    open var nome: String,
    open var id: Long,
    var idVisual: String = "",
    var processar: JFXButton? = null,
    var site: JFXButton? = null,
    var imagem: ImageView? = null,
    var isSelecionado: Boolean = false,
    var isMarcado: Boolean = false
) {

    constructor(descricao: String, nome: String, id: Long, processar: Boolean) :
    this(descricao, nome, id) {
        idVisual = id?.toString() ?: ""
        this.isMarcado = processar
    }

    fun setButton(processar: JFXButton, site: JFXButton) {
        this.processar = processar
        this.site = site
    }
}