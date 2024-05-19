package br.com.fenix.processatexto.controller.novels

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTreeTableCellCustom
import br.com.fenix.processatexto.controller.BaseController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.Novel
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.processar.ProcessarNovels
import br.com.fenix.processatexto.service.NovelServices
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.robot.Robot
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*


class NovelsProcessarController : Initializable, BaseController {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var btnCarregar: JFXButton

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var cbBase: JFXComboBox<String>

    @FXML
    private lateinit var txtNovel: JFXTextField

    @FXML
    private lateinit var ckbProcessados: JFXCheckBox

    @FXML
    private lateinit var ckbMarcarTodos: JFXCheckBox

    @FXML
    private lateinit var treeBases: TreeTableView<Novel>

    @FXML
    private lateinit var treecMacado: TreeTableColumn<Novel, Boolean>

    @FXML
    private lateinit var treecBase: TreeTableColumn<Novel, String>

    @FXML
    private lateinit var treecNovel: TreeTableColumn<Novel, String>

    @FXML
    private lateinit var treecVolume: TreeTableColumn<Novel, Float>

    @FXML
    private lateinit var treecCapitulo: TreeTableColumn<Novel, Float>

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var barraProgressoVolumes: ProgressBar

    @FXML
    lateinit var barraProgressoCapitulos: ProgressBar

    private var novels: ProcessarNovels? = null
    private var service = NovelServices()
    private var TABELAS: ObservableList<NovelTabela>? = null

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
        btnProcessar.accessibleText = "PROCESSANDO"
        btnProcessar.text = "Pausar"
        btnCarregar.isDisable = true
        if (novels == null)
            novels = ProcessarNovels(this)
        treeBases.isDisable = true
        MenuPrincipalController.controller.getLblLog().text = "Iniciando o processamento das novels..."
        novels!!.processarTabelas(TABELAS!!)
    }

    @FXML
    private fun onBtnCarregar() = carregar()

    @FXML
    private fun onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.root, ckbMarcarTodos.isSelected)
        treeBases.refresh()
    }

    @Override
    override fun habilitar() {
        treeBases.isDisable = false
        MenuPrincipalController.controller.getLblLog().text = ""
        btnProcessar.accessibleText = "PROCESSAR"
        btnProcessar.text = "Processar"
        btnCarregar.isDisable = false
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
        barraProgresso.progress = 0.0
        barraProgressoCapitulos.progress = 0.0
    }

    @get:Override
    override val root: AnchorPane get() = apRoot

    @get:Override
    override val barraProgresso: ProgressBar get() = barraProgressoVolumes

    override val stackPane: StackPane get() = controllerPai.stackPane

    private var PROCESSADOS: Boolean? = null
    private var BASE: String? = null
    private var NOVEL: String? = null
    private var LINGUAGEM: Language? = null
    private var DADOS: TreeItem<Novel>? = null

    private fun carregar() {
        MenuPrincipalController.controller.getLblLog().text = "Carregando dados das novels..."
        btnCarregar.isDisable = true
        btnProcessar.isDisable = true
        treeBases.isDisable = true
        PROCESSADOS = ckbProcessados.isSelected
        BASE = if (cbBase.value != null) cbBase.value.trim() else ""
        NOVEL = txtNovel.text.trim()
        LINGUAGEM = cbLinguagem.selectionModel.selectedItem
        barraProgressoCapitulos.progress = ProgressIndicator.INDETERMINATE_PROGRESS
        barraProgresso.progress = ProgressIndicator.INDETERMINATE_PROGRESS

        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val carregaItens: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    service = NovelServices()
                    TABELAS = FXCollections.observableArrayList(service.selectTabelas(!PROCESSADOS!!, false, BASE!!, LINGUAGEM!!, NOVEL!!))
                    DADOS = treeData
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }

                return null
            }

            @Override
            override fun succeeded() {
                Platform.runLater {
                    treeBases.setRoot(DADOS)
                    MenuPrincipalController.controller.getLblLog().text = ""
                    ckbMarcarTodos.isSelected = true
                    btnCarregar.isDisable = false
                    btnProcessar.isDisable = false
                    treeBases.isDisable = false
                    barraProgressoCapitulos.progress = 0.0
                    barraProgresso.progress = 0.0
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                }
            }
        }
        val t = Thread(carregaItens)
        t.start()
    }

    // Implementa um nivel por tipo
    private val treeData: TreeItem<Novel>
        private get() {
            val itmRoot: TreeItem<Novel> = TreeItem(Novel("...", ""))
            for (tabela in TABELAS!!) {
                tabela.novel = "..."
                val itmTabela: TreeItem<Novel> = TreeItem(tabela)
                var itmNovel: TreeItem<Novel>? = null
                var volumeAnterior = ""
                for (volume in tabela.volumes) {
                    // Implementa um nivel por tipo
                    if (!volume.novel.equals(volumeAnterior, true) || itmNovel == null) {
                        volumeAnterior = volume.novel
                        volume.base = tabela.base
                        itmNovel = TreeItem(Novel(tabela.base, volume.novel, "..."))
                        itmTabela.children.add(itmNovel)
                        itmTabela.isExpanded = true
                    }
                    volume.base = tabela.base
                    val itmVolume: TreeItem<Novel> = TreeItem(volume)
                    for (capitulo in volume.capitulos) {
                        capitulo.base = tabela.base
                        itmVolume.children.add(TreeItem(capitulo))
                    }
                    itmNovel.children.add(itmVolume)
                }
                itmRoot.children.add(itmTabela)
                itmRoot.isExpanded = true
            }
            return itmRoot
        }

    private fun marcarTodosFilhos(treeItem: TreeItem<Novel>, newValue: Boolean) {
        treeItem.value.isProcessar = newValue
        treeItem.children.forEach { treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue) }
    }

    private fun ativaTodosPai(treeItem: TreeItem<Novel>, newValue: Boolean) {
        if (treeItem.parent != null) {
            treeItem.parent.value.isProcessar = newValue
            ativaTodosPai(treeItem.parent, newValue)
        }
    }

    private fun editaColunas() {
        // ==== (CHECK-BOX) ===
        treecMacado.setCellValueFactory { param ->
            val treeItem: TreeItem<Novel> = param.value
            val item: Novel = treeItem.value
            val booleanProp = SimpleBooleanProperty(item.isProcessar)
            booleanProp.addListener { _, _, newValue ->
                item.isProcessar = newValue
                marcarTodosFilhos(treeItem, newValue)
                if (newValue) // Somente ativa caso seja true, pois ao menos um nó precisa estar ativo
                    ativaTodosPai(treeItem, newValue)
                treeBases.refresh()
            }
            booleanProp
        }
        treecMacado.setCellFactory {
            val cell: CheckBoxTreeTableCellCustom<Novel, Boolean> = CheckBoxTreeTableCellCustom()
            cell.alignment = Pos.CENTER
            cell
        }
    }

    private fun linkaCelulas() {
        treecMacado.cellValueFactory = TreeItemPropertyValueFactory("processar")
        treecBase.setCellValueFactory(TreeItemPropertyValueFactory("base"))
        treecNovel.setCellValueFactory(TreeItemPropertyValueFactory("novel"))
        treecVolume.setCellValueFactory(TreeItemPropertyValueFactory("volume"))
        treecCapitulo.setCellValueFactory(TreeItemPropertyValueFactory("capitulo"))
        treeBases.isShowRoot = false
        editaColunas()
    }

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
        cbBase.editor.textProperty().addListener { _, _, _ ->
            autoCompletePopup.filter { item -> item.lowercase(Locale.getDefault()).contains(cbBase.editor.text.lowercase(Locale.getDefault())) }
            if (autoCompletePopup.filteredSuggestions.isEmpty() || cbBase.showingProperty().get() || cbBase.editor.text.isEmpty())
                autoCompletePopup.hide()
            else
                autoCompletePopup.show(cbBase.editor)
        }
        cbBase.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtNovel.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        linkaCelulas()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(NovelsProcessarController::class.java)
        val fxmlLocate: URL get() = NovelsProcessarController::class.java.getResource("/view/novels/NovelProcessar.fxml")
    }
}