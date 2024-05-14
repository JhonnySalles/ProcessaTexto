package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Notificacao
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import com.jfoenix.controls.JFXTextField
import com.jfoenix.controls.events.JFXDialogEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.effect.BoxBlur
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.scene.text.Font
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.sql.SQLException
import java.util.*


class CorrecaoController : Initializable {

    @FXML
    lateinit var txtVocabulario: JFXTextField

    @FXML
    lateinit var txtTraducao: JFXTextField

    private var vocabServ: VocabularioJaponesServices? = null
    private var vocabulario: Optional<Vocabulario> = Optional.empty()
    private val robot: Robot = Robot()

    private fun onBtnCancelar() = limpar()

    private fun onBtnConfirmar() = salvar()

    private fun servico(): CorrecaoController {
        vocabServ = VocabularioJaponesServices()
        return this
    }

    private fun procurar(): CorrecaoController {
        if (txtVocabulario.text.trim().isNotEmpty()) {

            if (vocabServ == null)
                servico()

            try {
                if (vocabServ!!.existe(txtVocabulario.text)) {
                    vocabulario = vocabServ!!.select(txtVocabulario.text.trim())
                    carregar()
                } else {
                    txtVocabulario.unFocusColor = Color.RED
                    Notificacoes.notificacao(Notificacao.ERRO, "Vocabulário informado não encontrado.", txtVocabulario.text)
                }
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao carregar vocabulário.", txtVocabulario.text)
                txtVocabulario.unFocusColor = Color.RED
            }
        }
        return this
    }

    private fun salvar(): CorrecaoController {
        if (txtTraducao.text.trim().isNotEmpty()) {
            if (vocabServ == null)
                servico()

            try {
                atualiza()
                vocabServ!!.insertOrUpdate(vocabulario.get())
                Notificacoes.notificacao(Notificacao.SUCESSO, "Vocabulário salvo com sucesso.", vocabulario.get().portugues)
                limpar()
                txtVocabulario.requestFocus()
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar tradução.", txtTraducao.text)
            }
        } else txtVocabulario.unFocusColor = Color.RED
        return this
    }

    private fun carregar(): CorrecaoController {
        txtVocabulario.text = vocabulario.get().vocabulario
        txtTraducao.text = vocabulario.get().portugues
        return this
    }

    private fun atualiza(): CorrecaoController {
        vocabulario.get().portugues = txtTraducao.text.trim()
        return this
    }

    private fun limpar(): CorrecaoController {
        txtVocabulario.text = ""
        txtTraducao.text = ""
        vocabulario = Optional.empty()
        return this
    }

    private fun configuraListenert() {
        txtVocabulario.focusedProperty().addListener { _, oldVal, _ ->
            if (oldVal) {
                txtVocabulario.unFocusColor = Color.web("#106ebe")
                procurar()
            }
        }
        txtTraducao.focusedProperty().addListener { _, oldVal, _ -> if (oldVal) txtTraducao.unFocusColor = Color.web("#106ebe") }
        txtVocabulario.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtTraducao.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
    }

    @Override
    override fun initialize(location: URL, resources: ResourceBundle) {
        configuraListenert()
        servico()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CorrecaoController::class.java)

        private val STYLE_SHEET: String = AlertasPopup::class.java.getResource("/css/Dark_Theme.css").toExternalForm()

        fun abreTelaCorrecao(rootStackPane: StackPane, nodeBlur: Node) {
            try {
                val blur = BoxBlur(3.0, 3.0, 3)
                val dialogLayout = JFXDialogLayout()

                val dialog = JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER)
                val loader = FXMLLoader()
                loader.location = fxmlLocate
                val newAnchorPane: Parent = loader.load()
                val cnt: CorrecaoController = loader.getController()
                val titulo = Label("Tela de correção")
                titulo.font = Font.font(20.0)
                titulo.textFill = Color.web("#ffffff", 0.8)

                val botoes: MutableList<JFXButton> = mutableListOf()
                val btnConfirmar = JFXButton("Confirmar")
                btnConfirmar.setOnAction { cnt.onBtnConfirmar() }
                btnConfirmar.styleClass.add("background-Green2")
                botoes.add(btnConfirmar)
                val btnCancelar = JFXButton("Cancelar")
                btnCancelar.setOnAction { cnt.onBtnCancelar() }
                btnCancelar.styleClass.add("background-Red2")
                botoes.add(btnCancelar)
                val btnVoltar = JFXButton("Voltar")
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

        val fxmlLocate: URL get() = CorrecaoController::class.java.getResource("/view/Correcao.fxml")
    }
}