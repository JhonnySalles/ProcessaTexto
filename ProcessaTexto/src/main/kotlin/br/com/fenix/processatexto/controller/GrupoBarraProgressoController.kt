package br.com.fenix.processatexto.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.AnchorPane
import java.net.URL
import java.util.*


class GrupoBarraProgressoController : Initializable {

    // Sera usado para colorir as barras de progresso
    /*final PseudoClass textoAzul = PseudoClass.getPseudoClass("progress-TextAzul");
          final PseudoClass textoVerde = PseudoClass.getPseudoClass("progress-TextVerde");
          final PseudoClass bProgressAzul = PseudoClass.getPseudoClass("progress-barAzul");
          final PseudoClass bProgressVerde = PseudoClass.getPseudoClass("progress-barVerde");*/

    @FXML
    lateinit var background: AnchorPane

    @FXML
    private lateinit var lblTitulo: Label

    @FXML
    private lateinit var lblLog: Label

    @FXML
    lateinit var barraProgresso: ProgressBar

    val titulo: Label get() = lblTitulo
    val log: Label get() = lblLog

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {}

    companion object {
        val fxmlLocate: URL get() = MenuPrincipalController::class.java.getResource("/view/GrupoBarraProgresso.fxml")
    }
}