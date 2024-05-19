package br.com.fenix.processatexto.controller.novels

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*


class NovelsController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    lateinit var stackPane: StackPane

    @FXML
    private lateinit var apConteinerRoot: AnchorPane

    @FXML
    private lateinit var importarController: NovelsImportarController

    @FXML
    private lateinit var processarController: NovelsProcessarController

    val root: AnchorPane get() = apConteinerRoot

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        importarController.controllerPai = this
        processarController.controllerPai = this
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(NovelsController::class.java)
        val fxmlLocate: URL get() = NovelsController::class.java.getResource("/view/novels/Novel.fxml")
    }
}