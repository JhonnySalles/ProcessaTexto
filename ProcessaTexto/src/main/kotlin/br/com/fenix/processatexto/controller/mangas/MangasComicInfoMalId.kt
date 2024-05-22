package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.model.entities.comicinfo.MAL
import br.com.fenix.processatexto.processar.comicinfo.ProcessaComicInfo
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import com.jfoenix.controls.JFXTextField
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.effect.BoxBlur
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.scene.text.Font
import javafx.util.Callback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.util.*


class MangasComicInfoMalId : Initializable {

    @FXML
    lateinit var txtId: JFXTextField

    @FXML
    lateinit var txtNome: JFXTextField

    @FXML
    lateinit var imagem: ImageView

    private val mal: MAL = MAL("", "")
    private val robot: Robot = Robot()
    private lateinit var consulta: MAL.Registro

    private fun validaCampos(): Boolean {
        val valida: Boolean = txtId.text.replace("/\\D+/g".toRegex(), "").trim().isNotEmpty()
        if (!valida)
            txtId.unFocusColor = Color.RED
        return valida
    }

    val id: String
        get() = if (validaCampos()) txtId.text else ""
    val objeto: MAL.Registro
        get() = consulta

    private fun configuraListenert() {
        txtId.focusedProperty().addListener { _, oldVal, _ ->
            if (oldVal) {
                if (validaCampos()) {
                    try {
                        txtNome.text = ""
                        imagem.image = null
                        txtId.unFocusColor = Color.web("#106ebe")
                        val numero = id.replace("/\\D+/g".toRegex(), "").toLong()
                        mal.myanimelist.clear()
                        consulta = mal.addRegistro("", numero, false)
                        ProcessaComicInfo.getById(numero, consulta)
                        txtNome.text = consulta.nome
                        imagem.image = consulta.imagem!!.image
                    } catch (e: Exception) {
                        LOGGER.error(e.message, e)
                    }
                }
            }
        }
        txtId.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtNome.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
    }

    @Override
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        configuraListenert()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MangasComicInfoMalId::class.java)

        private val STYLE_SHEET: String = AlertasPopup::class.java.getResource("/css/Dark_Theme.css").toExternalForm()
        private lateinit var btnConfirmar: JFXButton
        private lateinit var btnVoltar: JFXButton
        private lateinit var dialog: JFXDialog
        fun abreTelaCorrecao(rootStackPane: StackPane, nodeBlur: Node, callback: Callback<MAL.Registro, Boolean>) {
            try {
                val blur = BoxBlur(3.0, 3.0, 3)
                val dialogLayout = JFXDialogLayout()
                dialog = JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER)
                val loader = FXMLLoader()
                loader.location = fxmlLocate
                val newAnchorPane: Parent = loader.load()
                val cnt: MangasComicInfoMalId = loader.getController()
                val titulo = Label("Troca de Id")
                titulo.font = Font.font(20.0)
                titulo.textFill = Color.web("#ffffff", 0.8)
                val botoes = mutableListOf<JFXButton>()
                btnConfirmar = JFXButton("Confirmar")
                btnConfirmar.setOnAction {
                    val numero: String = cnt.id.replace("/\\D+/g".toRegex(), "")
                    if (numero.isNotEmpty()) {
                        callback.call(cnt.objeto)
                        dialog.close()
                    }
                }
                btnConfirmar.styleClass.add("background-Green2")
                botoes.add(btnConfirmar)
                btnVoltar = JFXButton("Voltar")
                btnVoltar.setOnAction { dialog.close() }
                btnVoltar.styleClass.add("background-White1")
                botoes.add(btnVoltar)
                dialogLayout.setHeading(titulo)
                dialogLayout.setBody(newAnchorPane)
                dialogLayout.setActions(botoes)
                dialog.stylesheets.add(STYLE_SHEET)
                dialog.padding = Insets(0.0, 0.0, 0.0, 0.0)
                dialog.setOnDialogClosed { nodeBlur.effect = null }
                nodeBlur.effect = blur
                dialog.show()
            } catch (e: IOException) {
                LOGGER.error(e.message, e)
            }
        }

        val fxmlLocate: URL get() = MangasComicInfoMalId::class.java.getResource("/view/mangas/MangaComicInfoMalId.fxml") as URL
    }
}