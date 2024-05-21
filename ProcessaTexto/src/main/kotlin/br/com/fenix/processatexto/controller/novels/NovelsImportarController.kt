package br.com.fenix.processatexto.controller.novels

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.notification.Alertas
import br.com.fenix.processatexto.controller.BaseController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.processar.ProcessarNovels
import br.com.fenix.processatexto.service.NovelServices
import br.com.fenix.processatexto.util.constraints.Validadores
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ProgressBar
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.util.*


class NovelsImportarController : Initializable, BaseController {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var cbBase: JFXComboBox<String>

    @FXML
    private lateinit var txtNovel: JFXTextField

    @FXML
    private lateinit var ckbFavorito: JFXCheckBox

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var txtCaminho: JFXTextField

    @FXML
    private lateinit var btnCaminho: JFXButton

    @FXML
    private lateinit var btnArquivo: JFXButton

    @FXML
    private lateinit var txtLog: JFXTextArea

    @FXML
    private lateinit var barraProgressoArquivos: ProgressBar

    @FXML
    lateinit var barraProgressoTextos: ProgressBar
    
    private var novels: ProcessarNovels? = null
    private val service = NovelServices()

    private lateinit var controller: NovelsController
    var controllerPai: NovelsController
        get() = controller
        set(controller) {
            this.controller = controller
        }


    @FXML
    private fun onBtnProcessar() {
        if (btnProcessar.accessibleText.equals("PROCESSANDO", true) && novels != null) {
            novels!!.setDesativar(true)
            return
        }
        
        if (!valida()) 
            return
        
        desabilitar()
        saveConfig()
        if (novels == null)
            novels = ProcessarNovels(this)
        MenuPrincipalController.controller.getLblLog().text = "Iniciando o processamento das novels..."
        novels!!.processarArquivos(File(txtCaminho.text), cbBase.editor.text, cbLinguagem.selectionModel.selectedItem, ckbFavorito.isSelected)
    }

    fun valida(): Boolean {
        if (txtCaminho.text.isEmpty()) {
            txtCaminho.unFocusColor = Color.RED
            return false
        }
        val caminho = File(txtCaminho.text)
        if (!caminho.exists()) {
            txtCaminho.unFocusColor = Color.RED
            return false
        }
        return true
    }

    fun desabilitar() {
        btnProcessar.accessibleText = "PROCESSANDO"
        btnProcessar.text = "Pausar"
    }

    @Override
    override fun habilitar() {
        MenuPrincipalController.controller.getLblLog().text = ""
        btnProcessar.accessibleText = "PROCESSAR"
        btnProcessar.text = "Processar"
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
        barraProgresso.progress = 0.0
        barraProgressoTextos.progress = 0.0
    }

    @FXML
    private fun onBtnCarregarCaminho() {
        txtCaminho.unFocusColor = Color.web("#106ebe")
        txtCaminho.text = selecionaPasta(txtCaminho.text, false)
    }

    @FXML
    private fun onBtnCarregarArquivo() {
        txtCaminho.unFocusColor = Color.web("#106ebe")
        txtCaminho.text = selecionaPasta(txtCaminho.text, true)
    }

    private fun selecionaPasta(local: String?, isArquivo: Boolean): String {
        var pasta = ""
        var caminho: File? = null
        if (local != null && local.isNotEmpty()) {
            caminho = File(local)
            if (caminho.isFile) {
                var file: String = caminho.absolutePath
                file = file.substring(0, file.indexOf(caminho.name))
                caminho = File(file)
            }
        }
        pasta = if (isArquivo) {
            val fileChooser = FileChooser()
            if (caminho != null)
                fileChooser.initialDirectory = caminho
            val extFilter: FileChooser.ExtensionFilter = FileChooser.ExtensionFilter("Arquivos", "*.txt")
            fileChooser.extensionFilters.add(extFilter)
            fileChooser.title = "Selecione o arquivo de destino"
            val file: File = fileChooser.showOpenDialog(null)
            if (file == null)
                ""
            else
                file.absolutePath
        } else {
            val fileChooser = DirectoryChooser()
            fileChooser.title = "Selecione a pasta de destino"
            if (caminho != null) fileChooser.initialDirectory = caminho
            val file: File = fileChooser.showDialog(null)
            if (file == null)
                ""
            else
                file.absolutePath
        }
        return pasta
    }

    private fun saveConfig() {
        if (txtCaminho.text == null || txtCaminho.text.trim().isEmpty()) return
        val caminho = File(txtCaminho.text)
        if (!caminho.exists()) return
        var pasta: File? = null
        pasta = if (caminho.isFile)
            File((caminho.parent + "\\" + CONFIG).replace("\\\\\\\\", "\\"))
        else
            File((caminho.path + "\\" + CONFIG).replace("\\\\\\\\", "\\"))
        val props = Properties()
        try {
            FileOutputStream(pasta).use { os ->
                props.clear()
                props.setProperty("base", cbBase.editor.text)
                props.setProperty("linguagem", cbLinguagem.selectionModel.selectedItem.toString())
                props.setProperty("favorito", if (ckbFavorito.isSelected) "sim" else "nao")
                props.setProperty("novel", txtNovel.text)
                props.store(os, "")
            }
        } catch (e: IOException) {
            Alertas.Tela_Alerta("Erro ao salvar o properties de configuração", e.message!!)
            LOGGER.error(e.message, e)
        }
    }

    fun loadConfig() {
        if (txtCaminho.text == null || txtCaminho.text.trim().isEmpty() || !File(txtCaminho.text).exists()) return
        val config = File((txtCaminho.text + "\\" + CONFIG).replace("\\\\\\\\", "\\"))
        if (config.exists()) {
            val props = Properties()
            try {
                FileInputStream(config).use { fs ->
                    props.load(fs)
                    cbBase.editor.text = props.getProperty("base")
                    cbLinguagem.selectionModel.select(Language.valueOf(props.getProperty("linguagem")))
                    ckbFavorito.isSelected = props.getProperty("favorito").equals("sim", true)
                    txtNovel.text = props.getProperty("novel")
                    cbBase.requestFocus()
                }
            } catch (e: IOException) {
                Alertas.Tela_Alerta("Erro ao carregar o properties de configuração", e.message!!)
                LOGGER.error(e.message, e)
            }
        }
    }

    fun addLog(text: String) = txtLog.appendText("$text")

    @get:Override
    override val root: AnchorPane get() = apRoot

    @get:Override
    override val barraProgresso: ProgressBar get() = barraProgressoArquivos

    override val stackPane: StackPane get() = controllerPai.stackPane

    private val robot: Robot = Robot()

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        cbLinguagem.items.addAll(Language.JAPANESE, Language.ENGLISH)
        cbLinguagem.selectionModel.selectFirst()
        try {
            cbBase.items.setAll(service.tabelas)
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
        val autoCompletePopup: JFXAutoCompletePopup<String> = JFXAutoCompletePopup()
        autoCompletePopup.suggestions.addAll(cbBase.items)
        autoCompletePopup.setSelectionHandler { event -> cbBase.setValue(event.getObject()) }
        cbBase.editor.textProperty().addListener {  _, _, _ ->
            autoCompletePopup.filter { item -> item.lowercase(Locale.getDefault()).contains(cbBase.editor.text.lowercase(Locale.getDefault())) }
            if (autoCompletePopup.filteredSuggestions.isEmpty() || cbBase.showingProperty().get() || cbBase.editor.text.isEmpty())
                autoCompletePopup.hide()
            else
                autoCompletePopup.show(cbBase.editor)
        }
        cbBase.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbBase.focusedProperty().addListener { _, oldValue, _ ->
            if (oldValue) {
                val base: String = cbBase.editor.text
                if (base != null) {
                    if (base.contains(" "))
                        cbBase.editor.text = base.replace(" ", "_").lowercase(Locale.getDefault())
                    else
                        cbBase.editor.text = base.lowercase(Locale.getDefault())
                }
            }
        }
        txtNovel.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtCaminho.focusedProperty().addListener { _, oldValue, _ -> if (oldValue) loadConfig() }
        Validadores.setTextFieldNotEmpty(txtCaminho)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(NovelsImportarController::class.java)
        private const val CONFIG = "processa.config"
        val fxmlLocate: URL get() = NovelsImportarController::class.java.getResource("/view/novels/NovelImportar.fxml") as URL
    }
}