package br.com.fenix.processatexto.controller

import javafx.scene.control.ProgressBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane


interface BaseController {
    val stackPane: StackPane
    val root: AnchorPane
    val barraProgresso: ProgressBar
    fun habilitar()
}