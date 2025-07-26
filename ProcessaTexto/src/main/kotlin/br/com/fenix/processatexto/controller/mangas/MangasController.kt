package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.service.SincronizacaoServices
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Tab
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

class MangasController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    lateinit var stackPane: StackPane

    @FXML
    lateinit var tbComicInfo: Tab

    @FXML
    protected lateinit var apConteinerRoot: AnchorPane

    @FXML
    private lateinit var jsonController: MangasJsonController

    @FXML
    private lateinit var processarController: MangasProcessarController

    @FXML
    private lateinit var ajustarController: MangasAjustarController

    @FXML
    private lateinit var traducaoController: MangasTraducaoController

    @FXML
    private lateinit var vincularController: MangasVincularController

    @FXML
    private lateinit var comicinfoController: MangasComicInfoController

    @FXML
    private fun onSelectComicInfoChanged(event: Event) {
        SincronizacaoServices.processarComicInfo = tbComicInfo.isSelected
        MangasComicInfoController.selecionado = tbComicInfo.isSelected
    }

    val root: AnchorPane get() = apConteinerRoot

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        jsonController.controllerPai = this
        processarController.controllerPai = this
        ajustarController.controllerPai = this
        traducaoController.controllerPai = this
        vincularController.controllerPai = this
        comicinfoController.controllerPai = this
    }

    companion object {
        private val oLog: Logger = LoggerFactory.getLogger(MangasController::class.java)
        val fxmlLocate: URL get() = MangasController::class.java.getResource("/view/mangas/Manga.fxml") as URL
    }
}