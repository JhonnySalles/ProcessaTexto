package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTreeTableCellCustom
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.MangaServices
import br.com.fenix.processatexto.util.Utils
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


class MangasTraducaoController : Initializable {

    private val LOGGER: Logger = LoggerFactory.getLogger(MangasTraducaoController::class.java)

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var cbBase: JFXComboBox<String>

    @FXML
    private lateinit var txtManga: JFXTextField

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var spnVolume: Spinner<Int>

    @FXML
    private lateinit var spnCapitulo: Spinner<Double>

    @FXML
    private lateinit var btnCarregar: JFXButton

    @FXML
    private lateinit var btnTraduzir: JFXButton

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
    private lateinit var treecLinguagem: TreeTableColumn<Manga, Language>

    @FXML
    private lateinit var treecVolume: TreeTableColumn<Manga, Int>

    @FXML
    private lateinit var treecCapitulo: TreeTableColumn<Manga, Float>

    private var service: MangaServices = MangaServices()
    private var TABELAS: ObservableList<MangaTabela> = FXCollections.observableArrayList()
    private var PAUSAR: Boolean = false

    private lateinit var controller: MangasController
    var controllerPai: MangasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    @FXML
    private fun onBtnCarregar() = carregar()

    @FXML
    private fun onBtnTraduzir() {
        if (btnTraduzir.accessibleText.equals("GERANDO", false)) {
            PAUSAR = true
            return
        }

        if (TABELAS.size === 0)
            AlertasPopup.AvisoModal("Aviso", "Nenhum item informado.")

        btnTraduzir.accessibleText = "GERANDO"
        btnTraduzir.text = "Pausar"
        btnCarregar.isDisable = true
        treeBases.isDisable = true
        traduzir()
    }

    @FXML
    private fun onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.root, ckbMarcarTodos.isSelected)
        treeBases.refresh()
    }

    fun habilitar() {
        treeBases.isDisable = false
        btnTraduzir.accessibleText = "GERAR"
        btnTraduzir.text = "Traduzir"
        btnCarregar.isDisable = false
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
    }

    private var X: Long = 0
    private var XSize: Long = 0
    private var P: Long = 0
    private var traducoes: Long = 0
    private var error: String = ""

    private fun traduzir() {
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        PAUSAR = false
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        progress!!.titulo.text = "Traduzindo..."
        val traduzir: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    error = ""
                    updateMessage("Traduzindo....")
                    traducoes = 0
                    for ((I, tabela) in TABELAS.withIndex()) {
                        if (!tabela.isProcessar) {
                            Platform.runLater {
                                if (TaskbarProgressbar.isSupported())
                                    TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), TABELAS.size.toLong(), Type.NORMAL)
                            }
                            continue
                        }
                        XSize = tabela.volumes.size.toLong()
                        tabela.volumes.forEach { t ->
                            if (t.isProcessar) {
                                XSize += t.capitulos.size
                                t.capitulos.forEach { c -> XSize += c.paginas.size }
                            }
                        }
                        X = 0
                        for (volume in tabela.volumes) {
                            X++
                            if (!volume.isProcessar) continue
                            val volumeTraduzido = MangaVolume(
                                null, volume.manga, volume.volume,
                                Language.PORTUGUESE_GOOGLE, volume.arquivo
                            )
                            for (capitulo in volume.capitulos) {
                                X++
                                if (!capitulo.isProcessar) continue
                                val capituloTraduzido = MangaCapitulo(
                                    null, capitulo.manga,
                                    capitulo.volume, capitulo.capitulo, Language.PORTUGUESE_GOOGLE,
                                    capitulo.scan, capitulo.isExtra, capitulo.isRaw
                                )
                                volumeTraduzido.addCapitulos(capituloTraduzido)
                                P = 0
                                for (pagina in capitulo.paginas) {
                                    P++
                                    X++
                                    updateProgress(X, XSize)
                                    updateMessage(
                                        (volume.manga + " - Volume " + volume.volume.toString()
                                                + " Capitulo " + capitulo.capitulo.toString() + " Página "
                                                + P + '/' + capitulo.paginas.size) + " - " + pagina.nomePagina
                                    )

                                    val paginaTraduzido = MangaPagina(null, pagina.nomePagina, pagina.numero, pagina.hash)
                                    capituloTraduzido.addPaginas(paginaTraduzido)
                                    for (texto in pagina.textos) {
                                        val textoTraduzido = MangaTexto(null, "", texto.sequencia, texto.x1, texto.y1, texto.x2, texto.y2)
                                        paginaTraduzido.addTexto(textoTraduzido)
                                        traducoes++
                                        if (traducoes > 3000) {
                                            traducoes = 0
                                            MenuPrincipalController.controller.contaGoogle = Utils.next(MenuPrincipalController.controller.contaGoogle)

                                        }
                                        textoTraduzido.texto = ScriptGoogle.translate(
                                            capitulo.lingua.sigla,
                                            Language.PORTUGUESE.sigla, texto.texto,
                                            MenuPrincipalController.controller.contaGoogle
                                        )

                                        if (PAUSAR)
                                            return null
                                    }
                                }
                            }
                            service.salvarTraducao(tabela.base, volumeTraduzido)
                        }
                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), TABELAS.size.toLong(), Type.NORMAL)
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    error = e.message!!
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                Platform.runLater {
                    progress.barraProgresso.progressProperty().unbind()
                    progress.log.textProperty().unbind()
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                    if (error.isNotEmpty())
                        AlertasPopup.ErroModal("Erro", error)
                    else if (!PAUSAR) {
                        AlertasPopup.AvisoModal("Aviso", "Tradução concluida.")
                        TABELAS.clear()
                        DADOS!!.children.clear()
                    }
                    habilitar()
                }
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread tradução: " + super.getMessage())
                print("Erro na thread tradução: " + super.getMessage())
            }
        }
        progress.barraProgresso.progressProperty().bind(traduzir.progressProperty())
        progress.log.textProperty().bind(traduzir.messageProperty())
        val t = Thread(traduzir)
        t.start()
    }

    private var BASE: String? = null
    private var MANGA: String? = null
    private var VOLUME: Int? = null
    private var CAPITULO: Float? = null
    private var LINGUAGEM: Language? = null
    private var DADOS: TreeItem<Manga>? = null
    private fun carregar() {
        MenuPrincipalController.controller.getLblLog().text = "Carregando..."

        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        btnCarregar.isDisable = true
        btnTraduzir.isDisable = true
        treeBases.isDisable = true
        BASE = if (cbBase.value != null) cbBase.value.trim() else ""
        MANGA = txtManga.text.trim()
        VOLUME = spnVolume.value
        CAPITULO = spnCapitulo.value.toFloat()
        LINGUAGEM = cbLinguagem.selectionModel.selectedItem

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val carregaItens: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    service = MangaServices()
                    TABELAS = FXCollections.observableArrayList(service.selectAll(BASE!!, MANGA!!, VOLUME!!, CAPITULO!!, LINGUAGEM))
                    DADOS = treeData
                } catch (e: SQLException) {
                    LOGGER.error(e.message, e)
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.succeeded()
                Platform.runLater {
                    treeBases.root = DADOS
                    MenuPrincipalController.controller.getLblLog().text = ""
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    ckbMarcarTodos.isSelected = true
                    btnCarregar.isDisable = false
                    btnTraduzir.isDisable = false
                    treeBases.setDisable(false)
                }
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de carregamento de itens: " + super.getMessage())
                print("Erro na thread de carregamento de itens: " + super.getMessage())
            }
        }
        val t = Thread(carregaItens)
        t.start()
    }

    // Implementa um nivel por tipo
    private val treeData: TreeItem<Manga>
        get() {
            val itmRoot: TreeItem<Manga> = TreeItem<Manga>(Manga("...", ""))
            for (tabela in TABELAS) {
                tabela.manga = "..."
                val itmTabela: TreeItem<Manga> = TreeItem<Manga>(tabela)
                var itmManga: TreeItem<Manga>? = null
                var itmLingua: TreeItem<Manga>? = null
                var volumeAnterior = ""
                var linguagemAnterior: Language? = null

                for (volume in tabela.volumes) {

                    // Implementa um nivel por tipo
                    if (!volume.manga.equals(volumeAnterior, false) || itmManga == null) {
                        volumeAnterior = volume.manga
                        itmManga = TreeItem<Manga>(Manga(tabela.base, volume.manga, "..."))
                        itmTabela.children.add(itmManga)
                        itmTabela.isExpanded = true
                        itmLingua = TreeItem<Manga>(Manga(tabela.base, volume.manga, volume.lingua.sigla.uppercase(Locale.getDefault())))
                        linguagemAnterior = volume.lingua
                        itmManga.children.add(itmLingua)
                    }
                    if (linguagemAnterior == null || volume.lingua.compareTo(linguagemAnterior) !== 0) {
                        itmLingua = TreeItem<Manga>(Manga(tabela.base, volume.manga, volume.lingua.sigla.uppercase(Locale.getDefault())))
                        linguagemAnterior = volume.lingua
                        itmManga.children.add(itmLingua)
                    }
                    volume.linguagem = linguagemAnterior.sigla.uppercase(Locale.getDefault())
                    volume.base = tabela.base
                    val itmVolume: TreeItem<Manga> = TreeItem<Manga>(volume)
                    for (capitulo in volume.capitulos) {
                        capitulo.base = tabela.base
                        capitulo.nomePagina = "..."
                        capitulo.linguagem = linguagemAnterior.sigla.uppercase(Locale.getDefault())
                        itmVolume.children.add(TreeItem(capitulo))
                    }
                    itmLingua!!.children.add(itmVolume)
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
        treecBase.cellValueFactory = TreeItemPropertyValueFactory("base")
        treecManga.cellValueFactory = TreeItemPropertyValueFactory("manga")
        treecLinguagem.cellValueFactory = TreeItemPropertyValueFactory("linguagem")
        treecVolume.cellValueFactory = TreeItemPropertyValueFactory("volume")
        treecCapitulo.cellValueFactory = TreeItemPropertyValueFactory("capitulo")
        treeBases.isShowRoot = false
        editaColunas()
    }

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL, arg1: ResourceBundle) {
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
            if (autoCompletePopup.filteredSuggestions.isEmpty() || cbBase.showingProperty().get()
                || cbBase.editor.text.isEmpty()
            ) autoCompletePopup.hide() else autoCompletePopup.show(cbBase.editor)
        }
        cbBase.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagem.items.addAll(Language.ENGLISH, Language.JAPANESE)
        cbLinguagem.selectionModel.selectFirst()
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ESCAPE))
                cbLinguagem.selectionModel.clearSelection()
            else if (ke.code.equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB)
        }
        txtManga.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnVolume.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnCapitulo.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnVolume.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0)
        spnCapitulo.valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 99999.0, 0.0, 1.0)
        linkaCelulas()
    }

    companion object {
        val fxmlLocate: URL get() = MangasTraducaoController::class.java.getResource("/view/mangas/MangaTraducao.fxml") as URL
    }
}