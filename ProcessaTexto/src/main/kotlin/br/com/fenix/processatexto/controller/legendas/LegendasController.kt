package br.com.fenix.processatexto.controller.legendas

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle


class LegendasController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    lateinit var stackPane: StackPane

    @FXML
    protected lateinit var apConteinerRoot: AnchorPane

    @FXML
    private lateinit var importarController: LegendasImportarController

    @FXML
    private lateinit var processarController: LegendasVocabularioController

    @FXML
    private lateinit var marcasController: LegendasMarcasController
    
    val root: AnchorPane get() = apConteinerRoot

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        importarController.controllerPai = this
        processarController.controllerPai = this
        marcasController.controllerPai = this
    }

    companion object {
        val fxmlLocate: URL get() = LegendasController::class.java.getResource("/view/legendas/Legendas.fxml") as URL
    }
}