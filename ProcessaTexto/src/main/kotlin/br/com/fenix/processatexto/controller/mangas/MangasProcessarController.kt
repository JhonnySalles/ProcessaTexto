package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTreeTableCellCustom
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.processar.ProcessarMangas
import br.com.fenix.processatexto.service.MangaServices
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
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
import javafx.scene.robot.Robot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.sql.SQLException
import java.util.*


class MangasProcessarController : Initializable {

    private val LOGGER: Logger = LoggerFactory.getLogger(MangasProcessarController::class.java)

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var btnCarregar: JFXButton

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var cbBase: JFXComboBox<String>

    @FXML
    private lateinit var txtManga: JFXTextField

    @FXML
    private lateinit var ckbProcessados: JFXCheckBox

    @FXML
    private lateinit var ckbMarcarTodos: JFXCheckBox

    @FXML
    private lateinit var treeBases: TreeTableView<Manga>

    @FXML
    private lateinit var treecMacado: TreeTableColumn<Manga, Boolean>

    @FXML
    private lateinit var treecBase: TreeTableColumn<Manga, String>

    @FXML
    private lateinit var treecManga: TreeTableColumn<Manga, String>

    @FXML
    private lateinit var treecVolume: TreeTableColumn<Manga, Int>

    @FXML
    private lateinit var treecCapitulo: TreeTableColumn<Manga, Float>

    @FXML
    private lateinit var treecPagina: TreeTableColumn<Manga, Int>

    @FXML
    private lateinit var treecNomePagina: TreeTableColumn<Manga, String>

    @FXML
    private lateinit var btnTransferir: JFXButton

    @FXML
    private lateinit var txtBaseOrigem: JFXTextField

    @FXML
    private lateinit var txtBaseDestino: JFXTextField

    @FXML
    private lateinit var ckbCriarBase: JFXCheckBox

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    lateinit var barraProgressoVolumes: ProgressBar

    @FXML
    lateinit var barraProgressoCapitulos: ProgressBar

    @FXML
    lateinit var barraProgressoPaginas: ProgressBar

    private var mangas: ProcessarMangas? = null
    private var service: MangaServices = MangaServices()
    private var TABELAS: ObservableList<MangaTabela> = FXCollections.observableArrayList()

    private lateinit var controller: MangasController
    var controllerPai: MangasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    @FXML
    private fun onBtnProcessar() {
        if (btnProcessar.accessibleText.equals("PROCESSANDO", true) && mangas != null) {
            mangas!!.setDesativar(true)
            return
        }
        btnProcessar.accessibleText = "PROCESSANDO"
        btnProcessar.text = "Pausar"
        btnCarregar.isDisable = true
        if (mangas == null) mangas = ProcessarMangas(this)
        treeBases.isDisable = true
        MenuPrincipalController.controller.getLblLog().text = "Iniciando o processamento dos mangas..."
        when (cbLinguagem.selectionModel.selectedItem) {
            Language.ENGLISH -> mangas!!.processarTabelasIngles(TABELAS)
            Language.JAPANESE -> mangas!!.processarTabelasJapones(TABELAS)
            else -> {}
        }
    }

    @FXML
    private fun onBtnCarregar() = carregar()

    @FXML
    private fun onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.root, ckbMarcarTodos.isSelected)
        treeBases.refresh()
    }

    @FXML
    private fun onBtnTransferir() {
        transferir()
    }

    fun habilitar() {
        treeBases.isDisable = false
        MenuPrincipalController.controller.getLblLog().text = ""
        btnProcessar.accessibleText = "PROCESSAR"
        btnProcessar.text = "Processar"
        btnCarregar.isDisable = false
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
        barraProgressoVolumes.progress = 0.0
        barraProgressoCapitulos.progress = 0.0
        barraProgressoPaginas.progress = 0.0
    }

    val root: AnchorPane get() = apRoot

    private var TABELA_ORIGEM: String = ""
    private var TABELA_DESTINO: String = ""
    private var I: Long = 0
    private var error: String = ""
    private fun transferir() {
        if (txtBaseOrigem.text.trim().equals(txtBaseDestino.text.trim(), true)) {
            AlertasPopup.AvisoModal(controller.stackPane, controller.root, mutableListOf(), "Aviso", "Favor informar outra base de destino.")
            return
        }

        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Mangas - Transferencia"
        progress.log.text = "Transferindo dados...."

        btnTransferir.isDisable = true
        TABELA_ORIGEM = txtBaseOrigem.text.trim()
        TABELA_DESTINO = txtBaseDestino.text.trim()
        if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())
        val transferir: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    error = ""
                    updateMessage("Carregando dados....")
                    val tabelas: MutableList<String> = mutableListOf()
                    var isFull = false
                    if (TABELA_ORIGEM.contains(".*")) {
                        isFull = true
                        TABELA_ORIGEM = TABELA_ORIGEM.replace(".*", "")
                        TABELA_ORIGEM += "."
                        TABELA_ORIGEM = TABELA_ORIGEM.substring(0, TABELA_ORIGEM.lastIndexOf(".")).replace(".", "")
                        tabelas.addAll(service.getTabelasTransferir(TABELA_ORIGEM, "*"))
                    } else if (TABELA_ORIGEM.contains(".")) {
                        tabelas.add(TABELA_ORIGEM.substring(TABELA_ORIGEM.indexOf(".")))
                        TABELA_ORIGEM = TABELA_ORIGEM.substring(0, TABELA_ORIGEM.indexOf(".")) + "."
                    } else {
                        tabelas.add(TABELA_ORIGEM)
                        TABELA_ORIGEM = ""
                    }
                    for (tabela in tabelas) {
                        val lista: List<MangaVolume> = service.selectDadosTransferir(TABELA_ORIGEM, tabela)
                        if (ckbCriarBase.isSelected) {
                            updateMessage("Criando a tabela....")
                            service.createTabela(tabela)
                        }
                        updateMessage("Transferindo dados....")
                        I = 0
                        for (volume in lista) {
                            updateMessage("Transferindo dados.... " + volume.manga)
                            I++
                            if (isFull)
                                service.insertDadosTransferir(tabela, volume)
                            else
                                service.insertDadosTransferir(TABELA_DESTINO, volume)
                            updateProgress(I, lista.size.toLong())
                            Platform.runLater {
                                if (TaskbarProgressbar.isSupported())
                                    TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, lista.size.toLong(), Type.NORMAL)
                            }
                        }
                    }
                } catch (e: SQLException) {
                    LOGGER.error(e.message, e)
                    error = e.message!!
                } catch (e: Error) {
                    LOGGER.error(e.message, e)
                    error = e.message!!
                }
                return null
            }

            @Override
            override fun succeeded() {
                Platform.runLater {
                    progress.barraProgresso.progressProperty().unbind()
                    progress.log.textProperty().unbind()
                    MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                    btnTransferir.isDisable = false
                    barraProgressoVolumes.progress = 0.0
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    if (error.isNotEmpty())
                        AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", error)
                    else
                        AlertasPopup.AvisoModal(controller.stackPane, controller.root, mutableListOf(), "Aviso", "Transferência concluida.")
                }
            }
        }
        progress.barraProgresso.progressProperty().bind(transferir.progressProperty())
        progress.log.textProperty().bind(transferir.messageProperty())
        val t = Thread(transferir)
        t.start()
    }

    private var PROCESSADOS: Boolean? = null
    private var BASE: String? = null
    private var MANGA: String? = null
    private var LINGUAGEM: Language? = null
    private var DADOS: TreeItem<Manga>? = null
    private fun carregar() {
        MenuPrincipalController.controller.getLblLog().text = "Carregando dados dos mangas..."
        btnCarregar.isDisable = true
        btnProcessar.isDisable = true
        treeBases.isDisable = true
        PROCESSADOS = ckbProcessados.isSelected
        BASE = if (cbBase.value != null) cbBase.value.trim() else ""
        MANGA = txtManga.text.trim()
        LINGUAGEM = cbLinguagem.selectionModel.selectedItem
        barraProgressoCapitulos.progress = -1.0
        barraProgressoVolumes.progress = -1.0
        if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val carregaItens: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    service = MangaServices()
                    TABELAS = FXCollections.observableArrayList(service.selectTabelas(!PROCESSADOS!!, false, BASE!!, LINGUAGEM!!, MANGA!!))
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
                    barraProgressoVolumes.progress = 0.0
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                }
            }
        }
        val t = Thread(carregaItens)
        t.start()
    }

    // Implementa um nivel por tipo
    private val treeData: TreeItem<Manga>
        get() {
            val itmRoot: TreeItem<Manga> = TreeItem(Manga("...", ""))
            for (tabela in TABELAS) {
                tabela.manga = "..."
                val itmTabela: TreeItem<Manga> = TreeItem(tabela)
                var itmManga: TreeItem<Manga>? = null
                var volumeAnterior = ""
                for (volume in tabela.volumes) {
                    // Implementa um nivel por tipo
                    if (!volume.manga.equals(volumeAnterior, true) || itmManga == null) {
                        volumeAnterior = volume.manga
                        volume.base = tabela.base
                        itmManga = TreeItem(Manga(tabela.base, volume.manga, "..."))
                        itmTabela.children.add(itmManga)
                        itmTabela.isExpanded = true
                    }
                    volume.base = tabela.base
                    val itmVolume: TreeItem<Manga> = TreeItem(volume)
                    for (capitulo in volume.capitulos) {
                        capitulo.base = tabela.base
                        capitulo.nomePagina = "..."
                        val itmCapitulo: TreeItem<Manga> = TreeItem(capitulo)
                        for (pagina in capitulo.paginas) {
                            pagina.addOutrasInformacoes(tabela.base, volume.manga, volume.volume, capitulo.capitulo, volume.lingua)
                            itmCapitulo.children.add(TreeItem(pagina))
                        }
                        itmVolume.children.add(itmCapitulo)
                    }
                    itmManga.children.add(itmVolume)
                }
                itmRoot.children.add(itmTabela)
                itmRoot.isExpanded = true
            }
            return itmRoot
        }

    private fun marcarTodosFilhos(treeItem: TreeItem<Manga>, newValue: Boolean) {
        treeItem.value.isProcessar = newValue
        treeItem.children.forEach { treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue) }
    }

    private fun ativaTodosPai(treeItem: TreeItem<Manga>, newValue: Boolean) {
        if (treeItem.parent != null) {
            treeItem.parent.value.isProcessar = newValue
            ativaTodosPai(treeItem.parent, newValue)
        }
    }

    private fun editaColunas() {
        // ==== (CHECK-BOX) ===
        treecMacado.setCellValueFactory { param ->
            val treeItem: TreeItem<Manga> = param.value
            val item: Manga = treeItem.value
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
            val cell: CheckBoxTreeTableCellCustom<Manga, Boolean> = CheckBoxTreeTableCellCustom()
            cell.alignment = Pos.CENTER
            cell
        }
    }

    private fun linkaCelulas() {
        treecMacado.cellValueFactory = TreeItemPropertyValueFactory("processar")
        treecBase.setCellValueFactory(TreeItemPropertyValueFactory("base"))
        treecManga.setCellValueFactory(TreeItemPropertyValueFactory("manga"))
        treecVolume.setCellValueFactory(TreeItemPropertyValueFactory("volume"))
        treecCapitulo.setCellValueFactory(TreeItemPropertyValueFactory("capitulo"))
        treecPagina.setCellValueFactory(TreeItemPropertyValueFactory("pagina"))
        treecNomePagina.setCellValueFactory(TreeItemPropertyValueFactory("nomePagina"))
        treeBases.isShowRoot = false
        editaColunas()
    }

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL, arg1: ResourceBundle) {
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
        txtManga.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        linkaCelulas()
    }

    companion object {
        val fxmlLocate: URL get() = MangasProcessarController::class.java.getResource("/view/mangas/MangaProcessar.fxml") as URL
    }
}