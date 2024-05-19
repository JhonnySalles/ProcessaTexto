package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTreeTableCellCustom
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.service.MangaServices
import br.com.fenix.processatexto.util.converter.FloatConverter
import br.com.fenix.processatexto.util.converter.IntegerConverter
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.TreeTableColumn.CellEditEvent
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.robot.Robot
import javafx.util.Callback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.sql.SQLException
import java.util.*


class MangasAjustarController : Initializable {

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
    private lateinit var btnSalvar: JFXButton

    @FXML
    private lateinit var ckbReprocessarDemais: JFXCheckBox

    @FXML
    private lateinit var ckbInverterOrdemTexto: JFXCheckBox

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
    private lateinit var treecVolumeOrigem: TreeTableColumn<Manga, Int>

    @FXML
    private lateinit var treecCapituloOrigem: TreeTableColumn<Manga, Float>

    @FXML
    private lateinit var treecVolumeDestino: TreeTableColumn<Manga, Int>

    @FXML
    private lateinit var treecCapituloDestino: TreeTableColumn<Manga, Float>

    @FXML
    private lateinit var treecExtra: TreeTableColumn<Manga, Boolean>

    @FXML
    private lateinit var treecPagina: TreeTableColumn<Manga, String>

    @FXML
    private lateinit var treecTexto: TreeTableColumn<Manga, String>

    private var service: MangaServices = MangaServices()
    private var TABELAS: ObservableList<MangaTabela> = FXCollections.observableArrayList()

    private lateinit var controller: MangasController
    var controllerPai: MangasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    @FXML
    private fun onBtnCarregar() = carregar()

    @FXML
    private fun onBtnSalvar() {
        if (!TABELAS.isEmpty())
            salvar()
        else
            AlertasPopup.AvisoModal("Aviso", "Nenhum item para salvar.")
    }

    @FXML
    private fun onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.root, ckbMarcarTodos.isSelected)
        treeBases.refresh()
    }

    private var BASE: String? = null
    private var MANGA: String? = null
    private var VOLUME: Int? = null
    private var CAPITULO: Float? = null
    private var LINGUAGEM: Language? = null
    private var DADOS: TreeItem<Manga>? = null

    private fun carregar() {
        MenuPrincipalController.controller.getLblLog().text = "Carregando informações..."
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        btnCarregar.isDisable = true
        btnSalvar.isDisable = true
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
                    TABELAS = FXCollections.observableArrayList(service.selectTabelasJson(BASE!!, MANGA!!, VOLUME!!, CAPITULO!!, LINGUAGEM!!, ckbInverterOrdemTexto.isSelected))
                    DADOS = treeData
                } catch (e: SQLException) {
                    LOGGER.error(e.message, e)
                    throw Exception(Mensagens.BD_ERRO_SELECT)
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
                    btnSalvar.isDisable = false
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

    private fun salvar() {
        MenuPrincipalController.controller.getLblLog().text = "Salvando as informações corrigidas..."
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        btnCarregar.isDisable = true
        btnSalvar.isDisable = true
        treeBases.isDisable = true

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val carregaItens: Task<Void> = object : Task<Void>() {
            @Override
            override fun call(): Void? {
                try {
                    for (tabela in TABELAS) {
                        for (volume in tabela.volumes) {
                            if (volume.isAlterado)
                                volume.volume = volume.volumeDestino!!
                            for (capitulo in volume.capitulos) {
                                if (capitulo.isAlterado || volume.isAlterado)
                                    capitulo.volume = volume.volume
                                if (capitulo.isAlterado)
                                    capitulo.capitulo = capitulo.capituloDestino!!
                            }
                        }
                    }
                    service.salvarAjustes(TABELAS)
                    TABELAS.clear()
                    DADOS!!.children.clear()
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
                    AlertasPopup.AvisoModal("Aviso", "Alterações salva com sucesso.")
                    MenuPrincipalController.controller.getLblLog().text = ""
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    ckbMarcarTodos.isSelected = true
                    btnCarregar.isDisable = false
                    btnSalvar.isDisable = false
                    treeBases.setDisable(false)
                }
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Falha ao executar a thread de salvamento de as informações corrigidas: " + super.getMessage())
            }
        }
        val t = Thread(carregaItens)
        t.start()
    }

    // Implementa um nivel por tipo
    private val treeData: TreeItem<Manga>
        private get() {
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
                    if (!volume.manga.equals(volumeAnterior, true) || itmManga == null) {
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
                    volume.addOutrasInformacoes(tabela.base, volume.manga, volume.volume, -1f, volume.lingua)
                    val itmVolume: TreeItem<Manga> = TreeItem<Manga>(volume)
                    for (capitulo in volume.capitulos) {
                        capitulo.addOutrasInformacoes(tabela.base, capitulo.manga, capitulo.volume, capitulo.capitulo, linguagemAnterior)
                        capitulo.nomePagina = "..."
                        val itmCapitulo: TreeItem<Manga> = TreeItem<Manga>(capitulo)
                        for (pagina in capitulo.paginas) {
                            pagina.addOutrasInformacoes(
                                tabela.base, capitulo.manga, capitulo.volume,
                                capitulo.capitulo, linguagemAnterior, pagina.numero, pagina.nomePagina,
                                "..."
                            )
                            val itmPagina: TreeItem<Manga> = TreeItem<Manga>(pagina)
                            for (texto in pagina.textos) {
                                texto.addOutrasInformacoes(
                                    tabela.base, capitulo.manga, capitulo.volume,
                                    capitulo.capitulo, linguagemAnterior, pagina.numero,
                                    pagina.nomePagina, texto.texto
                                )
                                itmPagina.children.add(TreeItem(texto))
                            }
                            itmCapitulo.children.add(itmPagina)
                        }
                        itmVolume.children.add(itmCapitulo)
                    }
                    itmLingua!!.children.add(itmVolume)
                    itmLingua.isExpanded = true
                }
                itmTabela.isExpanded = true
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

    private fun setCapitulosChildreen(treeItem: TreeItem<Manga>, capitulo: Float) {
        if (treeItem.value is MangaCapitulo && (treeItem.value as MangaCapitulo).isExtra)
            return

        if (treeItem.value.isProcessar) {
            treeItem.value.isAlterado = true
            treeItem.value.capituloDestino = capitulo
            treeItem.children.forEach { treeItemNivel2 -> setCapitulosChildreen(treeItemNivel2, capitulo) }
        }
    }

    private fun setVolumesChildreen(treeItem: TreeItem<Manga>, volume: Int) {
        if (treeItem.value is MangaCapitulo && (treeItem.value as MangaCapitulo).isExtra)
            return

        if (treeItem.value.isProcessar) {
            treeItem.value.isAlterado = true
            treeItem.value.volumeDestino = volume
            treeItem.children.forEach { treeItemNivel2 -> setVolumesChildreen(treeItemNivel2, volume) }
        }
    }

    private var i: Int = 0
    private fun deletaCapitulo(treeItem: TreeItem<Manga>?) {
        if (treeItem != null && treeItem.value != null) {
            if (treeItem.value is MangaCapitulo || treeItem.value is MangaPagina || treeItem.value is MangaTexto) {
                var descricao = ""
                descricao = when (treeItem.value) {
                    is MangaCapitulo -> "Deseja remover o capítulo selecionado? ${treeItem.value.capitulo}"
                    is MangaPagina -> "Deseja remover a página selecionada? ${treeItem.value.nomePagina}"
                    else -> "Deseja remover o texto selecionado? ${treeItem.value.texto}"
                }

                if (AlertasPopup.ConfirmacaoModal("Apagar", descricao)) {
                    when (treeItem.value) {
                        is MangaCapitulo -> {
                            val volume: MangaVolume = treeItem.parent.value as MangaVolume
                            volume.capitulos.remove(treeItem.value)
                            volume.isAlterado = true
                            volume.isItemExcluido = true
                        }
                        is MangaPagina -> {
                            val capitulo: MangaCapitulo = treeItem.parent.value as MangaCapitulo
                            capitulo.paginas.remove(treeItem.value)
                            i = 1
                            capitulo.paginas.forEach { t -> t.numero = i++ }
                            capitulo.isAlterado = true
                            capitulo.isItemExcluido = true
                        }
                        else -> {
                            val pagina: MangaPagina = treeItem.parent.value as MangaPagina
                            pagina.textos.remove(treeItem.value)
                            i = 1
                            pagina.textos.forEach { t -> t.sequencia = i++ }
                            pagina.isItemExcluido = true
                            val capitulo: MangaCapitulo = treeItem.parent.parent.value as MangaCapitulo
                            capitulo.isAlterado = true
                            capitulo.isItemExcluido = true
                        }
                    }
                    treeItem.parent.children.remove(treeItem)
                    treeBases.refresh()
                }
            }
        }
    }

    private fun ajustaCapitulosDoVolume(treeItem: TreeItem<Manga>, volume: Int) {
        if (treeItem.value is MangaVolume)
            setVolumesChildreen(treeItem, volume)
        else if (treeItem.value is MangaCapitulo) {
            val treeLanguage: TreeItem<Manga> = treeItem.parent.parent
            val capitulo: MangaCapitulo = treeItem.value as MangaCapitulo
            val origem: MangaVolume = treeItem.parent.value as MangaVolume
            origem.capitulos.remove(capitulo)
            val position: Int = volume.compareTo(capitulo.volumeDestino!!)
            capitulo.volumeDestino = volume
            capitulo.isAlterado = true
            val treeNext: TreeItem<Manga> = if (position > 0)
                treeItem.nextSibling()
            else if (position < 0)
                treeItem.previousSibling()
            else
                return

            var destino: MangaVolume? = null
            for (tabela in TABELAS)
                if (tabela.volumes.contains(origem)) {
                    destino = tabela.volumes.stream()
                        .filter {
                            it.volume.compareTo(volume) === 0 && it.manga.equals(origem.manga, true) && it.lingua.compareTo(origem.lingua) === 0
                        }
                        .findFirst().orElse(
                            MangaVolume(
                                null, origem.manga, volume, origem.lingua,
                                origem.arquivo, ArrayList<MangaCapitulo>()
                            )
                        )
                    destino.addCapitulos(capitulo)
                    if (destino.getId() == null)
                        tabela.volumes.add(destino)
                    break
                }
            if (destino!!.getId() != null) {
                for (language in treeLanguage.children) {
                    if (language.value.volumeDestino!! == volume) {
                        treeItem.parent.children.remove(treeItem)
                        if (position < 0)
                            language.children.add(treeItem)
                        else
                            language.children.add(0, treeItem)
                        treeItem.value.volumeDestino = volume
                        break
                    }
                }
            } else {
                treeItem.parent.children.remove(treeItem)
                destino.addOutrasInformacoes(capitulo.base, capitulo.manga, volume, -1f, capitulo.lingua)
                val itmVolume: TreeItem<Manga> = TreeItem<Manga>(destino)
                itmVolume.children.add(treeItem)
                treeLanguage.children.add(itmVolume)
                destino.setId(null)
            }

            setVolumesChildreen(treeItem, volume)
            if (ckbReprocessarDemais.isSelected && treeNext != null) {
                if (treeNext.value is MangaCapitulo) {
                    val next: MangaCapitulo = treeNext.value as MangaCapitulo
                    if (origem.volumeDestino!!.compareTo(next.volumeDestino!!) !== 0)
                        return

                    if (position > 0 && next.capituloDestino!!.compareTo(capitulo.capituloDestino!!) > 0 || position < 0 && next.capituloDestino!!.compareTo(capitulo.capituloDestino!!) < 0)
                        ajustaCapitulosDoVolume(treeNext, volume)
                }
            }
        }
    }

    private fun ajustaCapitulosAnteriores(original: Manga, treeItem: TreeItem<Manga>?, quantidade: Int) {
        if (treeItem == null || !treeItem.value.equals(original) || treeItem.value.volume.compareTo(original.volume) !== 0)
            return

        setCapitulosChildreen(treeItem, treeItem.value.capituloDestino!! - quantidade)
        ajustaCapitulosAnteriores(original, treeItem.previousSibling(), quantidade)
    }

    private fun ajustaCapitulosPosteriores(original: Manga, treeItem: TreeItem<Manga>?, quantidade: Int) {
        if (treeItem == null || treeItem.value.volumeDestino!!.compareTo(original.volumeDestino!!) !== 0)
            return

        setCapitulosChildreen(treeItem, treeItem.value.capituloDestino!! + quantidade)
        ajustaCapitulosPosteriores(original, treeItem.nextSibling(), quantidade)
    }

    private fun editaColunas() {
        // ==== (CHECK-BOX) ===
        treecMacado.cellValueFactory =
            Callback<TreeTableColumn.CellDataFeatures<Manga, Boolean>, ObservableValue<Boolean>> { param ->
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
        treecExtra.setCellValueFactory { param ->
            val treeItem: TreeItem<Manga> = param.value
            if (treeItem.value is MangaCapitulo) {
                val item: MangaCapitulo = treeItem.value as MangaCapitulo
                val booleanProp = SimpleBooleanProperty(item.isExtra)
                booleanProp.addListener { _, _, newValue ->
                    item.isExtra = newValue
                    item.isAlterado = true
                }
                booleanProp
            } else null
        }
        treecExtra.setCellFactory { p ->
            val checkbox = object : CheckBoxTreeTableCellCustom<Manga, Boolean>() {
                @Override
                override fun updateItem(item: Boolean, empty: Boolean) {
                    val treeItem: TreeItem<Manga> = p.treeTableView.getTreeItem(index)
                    if (treeItem?.value != null && treeItem.value is MangaCapitulo)
                        super.updateItem(item, empty)
                    else {
                        text = null
                        graphic = null
                    }
                }
            }
            checkbox.alignment = Pos.CENTER
            checkbox
        }

        treecVolumeDestino.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(IntegerConverter()))
        treecVolumeDestino.setOnEditCommit(object : EventHandler<CellEditEvent<Manga, Int>> {
            @Override
            override fun handle(event: CellEditEvent<Manga, Int>) {
                val row: TreeItem<Manga> = treeBases.getTreeItem(event.treeTablePosition.row)
                if (event.newValue == null || row == null)
                    return

                if (row.value is MangaCapitulo || row.value is MangaVolume) {
                    val value: Int = event.oldValue!!.compareTo(event.newValue!!)
                    if (ckbReprocessarDemais.isSelected) {
                        if (value != 0)
                            ajustaCapitulosDoVolume(row, event.newValue)
                    } else {
                        row.value.volumeDestino = event.newValue
                        row.value.isAlterado = true
                    }
                }
                treeBases.refresh()
            }
        })

        treecCapituloDestino.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(FloatConverter()))
        treecCapituloDestino.setOnEditCommit(object : EventHandler<CellEditEvent<Manga, Float>> {
            @Override
            override fun handle(event: CellEditEvent<Manga, Float>) {
                val row: TreeItem<Manga> = treeBases.getTreeItem(event.treeTablePosition.row)
                if (event.newValue == null || row == null)
                    return

                if (row.value is MangaCapitulo) {
                    val diferenca: Int = (event.newValue - event.oldValue).toInt()
                    if (ckbReprocessarDemais.isSelected) {
                        if ((row.value as MangaCapitulo).isExtra) {
                            row.value.isAlterado = true
                            row.value.capituloDestino = event.newValue
                        } else {
                            setCapitulosChildreen(row, event.newValue)
                            ajustaCapitulosAnteriores(row.value, row.previousSibling(), diferenca * -1)
                            ajustaCapitulosPosteriores(row.value, row.nextSibling(), diferenca)
                        }
                    }
                }
                treeBases.refresh()
            }
        })
    }

    private fun linkaCelulas() {
        treecMacado.setCellValueFactory(TreeItemPropertyValueFactory("processar"))
        treecBase.setCellValueFactory(TreeItemPropertyValueFactory("base"))
        treecManga.setCellValueFactory(TreeItemPropertyValueFactory("manga"))
        treecLinguagem.setCellValueFactory(TreeItemPropertyValueFactory("linguagem"))
        treecVolumeOrigem.setCellValueFactory(TreeItemPropertyValueFactory("volume"))
        treecCapituloOrigem.setCellValueFactory(TreeItemPropertyValueFactory("capitulo"))
        treecVolumeDestino.setCellValueFactory(TreeItemPropertyValueFactory("volumeDestino"))
        treecCapituloDestino.setCellValueFactory(TreeItemPropertyValueFactory("capituloDestino"))
        treecExtra.setCellValueFactory(TreeItemPropertyValueFactory("isExtra"))
        treecPagina.setCellValueFactory(TreeItemPropertyValueFactory("nomePagina"))
        treecTexto.setCellValueFactory(TreeItemPropertyValueFactory("texto"))
        treeBases.isShowRoot = false
        treeBases.isEditable = true
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
        cbBase.editor.textProperty().addListener { _, _, _ ->
            autoCompletePopup.filter { item -> item.lowercase(Locale.getDefault()).contains(cbBase.editor.text.lowercase(Locale.getDefault())) }
            if (autoCompletePopup.filteredSuggestions.isEmpty() || cbBase.showingProperty().get()
                || cbBase.editor.text.isEmpty()
            ) autoCompletePopup.hide() else autoCompletePopup.show(cbBase.editor)
        }
        cbBase.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagem.items.addAll(
            Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
            Language.PORTUGUESE_GOOGLE
        )
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ESCAPE))
                cbLinguagem.selectionModel.clearSelection()
            else if (ke.code.equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB)
        }
        txtManga.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnVolume.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnCapitulo.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        treeBases.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.DELETE))
                deletaCapitulo(treeBases.selectionModel.selectedItem)
        }
        spnVolume.setValueFactory(SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0))
        spnCapitulo.setValueFactory(SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 99999.0, 0.0, 1.0))
        linkaCelulas()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MangasAjustarController::class.java)
        val fxmlLocate: URL
            get() = MangasAjustarController::class.java.getResource("/view/mangas/MangaAjustar.fxml")
        val iconLocate: String
            get() = "/images/icoTextoJapones_128.png"
    }
}