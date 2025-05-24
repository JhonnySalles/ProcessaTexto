package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTreeTableCellCustom
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.comicinfo.BaseLista
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.entities.comicinfo.MAL
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.processar.comicinfo.ProcessaComicInfo
import br.com.fenix.processatexto.service.ComicInfoServices
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXTextField
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.css.PseudoClass
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.MenuItem
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.robot.Robot
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.util.Callback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.stream.Collectors


class MangasComicInfoController : Initializable {

    val comicRoot: PseudoClass = PseudoClass.getPseudoClass("comic-info")
    val comicSelecionado: PseudoClass = PseudoClass.getPseudoClass("comic-selected")


    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var txtCaminho: JFXTextField

    @FXML
    private lateinit var txtDescricaoCapitulo: JFXTextField

    @FXML
    private lateinit var btnCaminho: JFXButton

    @FXML
    private lateinit var btnArquivo: JFXButton

    @FXML
    private lateinit var btnProcessarArquivos: JFXButton

    @FXML
    private lateinit var btnValidarComicInfo: JFXButton

    @FXML
    private lateinit var btnSalvarMarcados: JFXButton

    @FXML
    private lateinit var btnLimparLista: JFXButton

    @FXML
    private lateinit var cbIgnorarVinculoSalvo: JFXCheckBox

    @FXML
    private lateinit var treeTabela: TreeTableView<BaseLista>

    @FXML
    private lateinit var treecMacado: TreeTableColumn<BaseLista, Boolean>

    @FXML
    private lateinit var treecManga: TreeTableColumn<BaseLista, String>

    @FXML
    private lateinit var treecNome: TreeTableColumn<BaseLista, String>

    // ----------------------------------------MY ANIME LIST ---------------------------------------------------------

    @FXML
    private lateinit var treecMalID: TreeTableColumn<BaseLista, String>

    @FXML
    private lateinit var treecProcessar: TreeTableColumn<BaseLista, String>

    @FXML
    private lateinit var treecMyAnimeList: TreeTableColumn<BaseLista, String>

    @FXML
    private lateinit var treecImagem: TreeTableColumn<BaseLista, ImageView>

    private val mREGISTROS: ObservableList<MAL> = FXCollections.observableArrayList()

    private lateinit var controller: MangasController
    var controllerPai: MangasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    @FXML
    private fun onBtnProcessarArquivos() {
        if (btnProcessarArquivos.accessibleTextProperty().value.equals("PROCESSAR"))
            processar()
        else
            cancelar()
    }

    @FXML
    private fun onBtnValidarComicInfo() {
        if (btnValidarComicInfo.accessibleTextProperty().value.equals("VALIDAR"))
            validar()
        else
            cancelar()
    }

    @FXML
    private fun onBtnSalvarComicInfo() {
        if (btnSalvarMarcados.accessibleTextProperty().value.equals("PROCESSAR"))
            processarLista(false)
        else
            mPARAR = true
    }

    @FXML
    private fun onBtnCarregarCaminho() {
        txtCaminho.text = selecionaPasta(txtCaminho.text, false)
    }

    @FXML
    private fun onBtnCarregarArquivo() {
        txtCaminho.text = selecionaPasta(txtCaminho.text, true)
    }

    @FXML
    private fun onBtnLimparLista() {
        mREGISTROS.clear()
        configuraTabela()
    }

    private fun ativaCampos() {
        treeTabela.isDisable = false
        btnSalvarMarcados.isDisable = false
        btnProcessarArquivos.isDisable = false
        btnValidarComicInfo.isDisable = false
        btnLimparLista.isDisable = false
    }

    private fun bloqueiaCampos(origem: String) {
        btnLimparLista.isDisable = true
        treeTabela.isDisable = true
        if (origem === "PROCESSAR") {
            btnSalvarMarcados.isDisable = true
            btnValidarComicInfo.isDisable = true
        } else if (origem === "VALIDAR") {
            btnSalvarMarcados.isDisable = true
            btnProcessarArquivos.isDisable = true
        } else if (origem === "MARCADOS") {
            btnProcessarArquivos.isDisable = true
            btnValidarComicInfo.isDisable = true
        }
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
            if (caminho != null) fileChooser.initialDirectory = caminho
            val extFilter: FileChooser.ExtensionFilter = FileChooser.ExtensionFilter("Arquivos", "*.cbr", "*.cbz", "*.rar", "*.zip")
            fileChooser.extensionFilters.add(extFilter)
            fileChooser.title = "Selecione o arquivo de destino"
            val file = fileChooser.showOpenDialog(null)
            if (file == null) "" else file.absolutePath
        } else {
            val fileChooser = DirectoryChooser()
            fileChooser.title = "Selecione a pasta de destino"
            if (caminho != null) fileChooser.initialDirectory = caminho
            val file = fileChooser.showDialog(null)
            if (file == null) "" else file.absolutePath
        }
        return pasta
    }

    private fun cancelar() = ProcessaComicInfo.cancelar()

    private fun processar() {
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        progress!!.titulo.text = "ComicInfo"
        val gerarJson: Task<Void> = object : Task<Void>() {
            @Override
            override fun call(): Void? {
                try {
                    updateMessage("Processando itens....")
                    val callback: Callback<Array<Long>, Boolean> = Callback<Array<Long>, Boolean> { param ->
                        Platform.runLater {
                            updateMessage("Processando itens...." + param[0] + '/' + param[1])
                            updateProgress(param[0], param[1])
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), param[0], param[1], Type.NORMAL)
                        }
                        null
                    }
                    ProcessaComicInfo.processa(cbLinguagem.value, txtCaminho.text, txtDescricaoCapitulo.text, cbIgnorarVinculoSalvo.isSelected, callback)
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                Platform.runLater {
                    ativaCampos()
                    btnProcessarArquivos.accessibleText = "PROCESSAR"
                    btnProcessarArquivos.text = "Processar comic info"
                    progress.barraProgresso.progressProperty().unbind()
                    progress.log.textProperty().unbind()
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                }
            }

            @Override
            override fun failed() {
                super.failed()
                ativaCampos()
                btnProcessarArquivos.accessibleText = "PROCESSAR"
                btnProcessarArquivos.text = "Processar comic info"
                LOGGER.warn("Erro na thread ComicInfo: " + super.getMessage())
                println("Erro na thread ComicInfo: " + super.getMessage())
            }
        }
        progress.barraProgresso.progressProperty().bind(gerarJson.progressProperty())
        progress.log.textProperty().bind(gerarJson.messageProperty())
        val t = Thread(gerarJson)
        t.start()
        btnProcessarArquivos.text = "Cancelar"
        btnProcessarArquivos.accessibleText = "PROCESSANDO"
        bloqueiaCampos("PROCESSAR")
    }

    private var mPARAR = false
    private fun processarLista(isSelecionado: Boolean) {
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())
        progress!!.titulo.text = "ComicInfo"
        val gerarJson: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    updateMessage("Processando itens....")
                    mPARAR = false
                    val lista: List<MAL> = if (isSelecionado)
                        mREGISTROS.parallelStream().filter { it.isSelecionado || it.myAnimeList.parallelStream().anyMatch(BaseLista::isSelecionado) }.collect(Collectors.toList())
                    else
                        mREGISTROS.parallelStream().collect(Collectors.toList())

                    val comicService = ComicInfoServices()

                    for ((I, item) in lista.withIndex()) {
                        Platform.runLater {
                            updateMessage("Processando itens...." + I + '/' + lista.size + " - Manga: " + item.nome)
                            updateProgress(I.toLong(), lista.size.toLong())
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), lista.size.toLong(), Type.NORMAL)
                        }

                        val registro: Optional<MAL.Registro> = item.myAnimeList.stream().filter(BaseLista::isMarcado).findFirst()
                        if (registro.isPresent) {
                            if (ProcessaComicInfo.processa(cbLinguagem.value, registro.get().parent.arquivo, registro.get().id))
                                mREGISTROS.remove(item)
                        }

                        if (mPARAR)
                            break
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                Platform.runLater {
                    ativaCampos()
                    configuraTabela()
                    btnSalvarMarcados.accessibleText = "PROCESSAR"
                    btnSalvarMarcados.text = "Processar Marcados"
                    progress.barraProgresso.progressProperty().unbind()
                    progress.log.textProperty().unbind()
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                }
            }

            @Override
            override fun failed() {
                super.failed()
                ativaCampos()
                configuraTabela()
                btnSalvarMarcados.accessibleText = "PROCESSAR"
                btnSalvarMarcados.text = "Processar Marcados"
                LOGGER.warn("Erro na thread ComicInfo: " + super.getMessage())
                println("Erro na thread ComicInfo: " + super.getMessage())
            }
        }
        progress.barraProgresso.progressProperty().bind(gerarJson.progressProperty())
        progress.log.textProperty().bind(gerarJson.messageProperty())
        val t = Thread(gerarJson)
        t.start()
        btnSalvarMarcados.text = "Cancelar"
        btnSalvarMarcados.accessibleText = "PROCESSANDO"
        bloqueiaCampos("MARCADOS")
    }

    private fun validar() {
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        progress!!.titulo.text = "ComicInfo"
        val gerarJson: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    updateMessage("Validando itens....")
                    val callback: Callback<Array<Long>, Boolean> = Callback<Array<Long>, Boolean> { param ->
                        Platform.runLater {
                            updateMessage("Validando itens...." + param[0] + '/' + param[1])
                            updateProgress(param[0], param[1])
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), param[0], param[1], Type.NORMAL)
                        }
                        null
                    }
                    ProcessaComicInfo.validar(cbLinguagem.value, txtCaminho.text, callback)
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                Platform.runLater {
                    ativaCampos()
                    btnValidarComicInfo.accessibleText = "VALIDAR"
                    btnValidarComicInfo.text = "Validar comic info"
                    progress.barraProgresso.progressProperty().unbind()
                    progress.log.textProperty().unbind()
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                }
            }

            @Override
            override fun failed() {
                super.failed()
                ativaCampos()
                btnValidarComicInfo.accessibleText = "VALIDAR"
                btnValidarComicInfo.text = "Validar comic info"
                LOGGER.warn("Erro na thread ComicInfo: " + super.getMessage())
                println("Erro na thread ComicInfo: " + super.getMessage())
            }
        }
        progress.barraProgresso.progressProperty().bind(gerarJson.progressProperty())
        progress.log.textProperty().bind(gerarJson.messageProperty())
        val t = Thread(gerarJson)
        t.start()
        btnValidarComicInfo.accessibleText = "VALIDANDO"
        btnValidarComicInfo.text = "Cancelar"
        bloqueiaCampos("VALIDAR")
    }

    private fun openSiteMal(id: Long) {
        try {
            Desktop.getDesktop().browse(URI("https://myanimelist.net/manga/$id"))
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
        } catch (e: URISyntaxException) {
            LOGGER.error(e.message, e)
        }
    }

    fun addItem(item: MAL?) {
        mREGISTROS.add(item)
        configuraTabela()
    }

    // ---------------- Adicionado na tabela ---------------- //
    private val treeData: TreeItem<BaseLista>
        get() {
            val itmRoot: TreeItem<BaseLista> = TreeItem<BaseLista>(BaseLista("...", "", -1, false))
            for (item in mREGISTROS) {
                val itmManga: TreeItem<BaseLista> = TreeItem<BaseLista>(item)

                // ---------------- Mal ---------------- //
                for (registro in item.myAnimeList) {
                    val reg: TreeItem<BaseLista> = TreeItem<BaseLista>(registro)
                    val processar = JFXButton("Processar")
                    processar.styleClass.add("background-White1")
                    processar.setOnAction {
                        val arquivo: String = registro.parent.arquivo
                        if (ProcessaComicInfo.processa(cbLinguagem.value, arquivo, registro.id)) {
                            mREGISTROS.remove(item)
                            itmRoot.children.remove(itmManga)
                            treeTabela.refresh()
                        }
                    }
                    val site = JFXButton("Site")
                    site.styleClass.add("background-White1")
                    site.setOnAction { openSiteMal(registro.id) }
                    reg.value.setMyAnimeListButton(processar, site)
                    itmManga.children.add(reg)
                }

                // ---------------- Adicionado na tabela ---------------- //
                itmRoot.children.add(itmManga)
                itmRoot.isExpanded = true
            }
            return itmRoot
        }
    private var mDADOS: TreeItem<BaseLista>? = null
    private fun configuraTabela() {
        try {
            mDADOS = treeData
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
        treeTabela.root = mDADOS
    }

    private fun onBtnTrocaId() {
        if (mREGISTROS.parallelStream().noneMatch { it.isSelecionado || it.myAnimeList.stream().anyMatch(BaseLista::isSelecionado) } && treeTabela.selectionModelProperty().value != null)
            treeTabela.selectionModelProperty().value.selectedItem.value.isSelecionado = true

        val callback: Callback<MAL.Registro, Boolean> = Callback<MAL.Registro, Boolean> { param ->
            mREGISTROS.parallelStream().filter { it.isSelecionado || it.myAnimeList.stream().anyMatch(BaseLista::isSelecionado) }
                .forEach {
                    if (it.isSelecionado) {
                        it.myAnimeList.parallelStream().forEach { re -> re.isMarcado = false }
                        val reg: MAL.Registro = it.myAnimeList[0]
                        reg.isMarcado = true
                        reg.id = param.id
                        reg.nome = param.nome
                        reg.imagem = param.imagem
                    } else {
                        it.myAnimeList.forEach { re -> re.isMarcado = false }
                        val reg: Optional<MAL.Registro> = it.myAnimeList.parallelStream()
                            .filter { re -> re.isSelecionado }.findFirst()
                        if (reg.isPresent) {
                            reg.get().isMarcado = true
                            reg.get().id = param.id
                            reg.get().nome = param.nome
                            reg.get().imagem = param.imagem
                        } else {
                            val aux: MAL.Registro = it.myAnimeList[0]
                            aux.isMarcado = true
                            aux.id = param.id
                            aux.nome = param.nome
                            aux.imagem = param.imagem
                        }
                    }
                    it.isSelecionado = false
                    it.myAnimeList.parallelStream().forEach { re -> re.isSelecionado = false }
                }
            treeTabela.refresh()
            null
        }
        MangasComicInfoMalId.abreTelaMal(controller.stackPane, controller.root, callback)
    }

    private fun editaColunas() {
        // ==== (CHECK-BOX) ===
        treecMacado.setCellValueFactory { param ->
            val treeItem: TreeItem<BaseLista> = param.value
            if (treeItem.value is MAL.Registro) {
                val item: MAL.Registro = treeItem.value as MAL.Registro
                val booleanProp = SimpleBooleanProperty(item.isMarcado)
                booleanProp.addListener { _, _, newValue ->
                    item.isMarcado = newValue
                    if (newValue) {
                        val parent: MAL = item.parent
                        for (aux in parent.myAnimeList) {
                            if (aux.id != item.id)
                                aux.isMarcado = false
                        }
                    }
                    treeTabela.refresh()
                }
                return@setCellValueFactory booleanProp
            }
            null
        }
        treecMacado.setCellFactory {
            val cell = CheckBoxTreeTableCellCustom<BaseLista, Boolean>()
            cell.alignment = Pos.CENTER
            cell
        }
    }

    private fun limparSelecao() {
        anteriorSelecionado = null
        mREGISTROS.parallelStream()
            .filter { it.isSelecionado || it.myAnimeList.stream().allMatch(BaseLista::isSelecionado) }
            .forEach {
                it.isSelecionado = false
                it.myAnimeList.parallelStream().forEach { re -> re.isSelecionado = false }
            }
        treeTabela.refresh()
    }

    private fun remover() {
        if (treeTabela.selectionModel.selectedItem != null)
            if (AlertasPopup.confirmacaoModal("Aviso", "Deseja remover o registro?")) {
            val parent: TreeItem<BaseLista> = if (treeTabela.selectionModel.selectedItem.value is MAL)
                treeTabela.selectionModel.selectedItem else treeTabela.selectionModel.selectedItem.parent
            mREGISTROS.remove(parent.value)
            parent.parent.children.remove(parent)
            treeTabela.refresh()
        }
    }

    private var anteriorSelecionado: TreeItem<BaseLista>? = null
    private fun selecionaRegistros() {
        treeTabela.setOnMouseClicked { click ->
            if (click.clickCount > 1) {
                if (click.isControlDown) {
                    limparSelecao()
                    return@setOnMouseClicked
                } else if (click.isShiftDown) {
                    if (anteriorSelecionado != null && anteriorSelecionado !== treeTabela.selectionModel.selectedItem) {
                        val parentAnterio: BaseLista = if (anteriorSelecionado!!.value is MAL)
                            anteriorSelecionado!!.value
                        else
                            anteriorSelecionado!!.parent.value
                        val parentAtual: BaseLista = if (treeTabela.selectionModel.selectedItem.value is MAL)
                            treeTabela.selectionModel.selectedItem.value
                        else
                            treeTabela.selectionModel.selectedItem.parent.value

                        var index: Int
                        var size: Int
                        if (mREGISTROS.indexOf(parentAnterio) > mREGISTROS.indexOf(parentAtual)) {
                            index = mREGISTROS.indexOf(parentAtual)
                            size = mREGISTROS.indexOf(parentAnterio) - 1
                        } else {
                            index = mREGISTROS.indexOf(parentAnterio) + 1
                            size = mREGISTROS.indexOf(parentAtual)
                        }
                        if (index < 0)
                            index = 0

                        if (size > mREGISTROS.size)
                            size = mREGISTROS.size

                        for (i in index..size)
                            mREGISTROS[i].isSelecionado = !mREGISTROS[i].isSelecionado
                    }
                } else {
                    val item: BaseLista? = treeTabela.selectionModel.selectedItem.value
                    if (item != null)
                        item.isSelecionado = !item.isSelecionado
                }
                treeTabela.refresh()
                anteriorSelecionado = treeTabela.selectionModel.selectedItem
            }
        }

        treeTabela.setRowFactory {
            val menu = ContextMenu()
            val alterarId = MenuItem("Alterar id")
            alterarId.setOnAction { onBtnTrocaId() }
            val processar = MenuItem("Processar selecionado(s)")
            processar.setOnAction { if (btnSalvarMarcados.accessibleTextProperty().value.equals("PROCESSAR")) processarLista(true) }
            val limparSelecao = MenuItem("Limpar seleção")
            limparSelecao.setOnAction { limparSelecao() }
            val remover = MenuItem("Remover registro")
            remover.setOnAction { remover() }
            menu.items.add(alterarId)
            menu.items.add(processar)
            menu.items.add(limparSelecao)
            menu.items.add(remover)
            val row: TreeTableRow<BaseLista> = object : TreeTableRow<BaseLista>() {
                @Override
                override fun updateItem(item: BaseLista?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null) {
                        style = ""
                        pseudoClassStateChanged(comicRoot, false)
                        pseudoClassStateChanged(comicSelecionado, false)
                    } else {
                        contextMenu = menu
                        if (item.isSelecionado) {
                            pseudoClassStateChanged(comicSelecionado, true)
                            pseudoClassStateChanged(comicRoot, false)
                        } else {
                            pseudoClassStateChanged(comicSelecionado, false)
                            pseudoClassStateChanged(comicRoot, item is MAL)
                        }
                    }
                }
            }
            row
        }
    }

    private fun linkaCelulas() {
        treecMacado.cellValueFactory = TreeItemPropertyValueFactory("isMarcado")
        treecManga.cellValueFactory = TreeItemPropertyValueFactory("descricao")
        treecNome.cellValueFactory = TreeItemPropertyValueFactory("nome")

        // ---------------- Mal ---------------- //
        treecMalID.cellValueFactory = TreeItemPropertyValueFactory("idVisual")
        treecProcessar.cellValueFactory = TreeItemPropertyValueFactory("processar")
        treecMyAnimeList.cellValueFactory = TreeItemPropertyValueFactory("myanimelist")

        treecImagem.cellValueFactory = TreeItemPropertyValueFactory("imagem")
        treeTabela.isShowRoot = false
        treecMalID.cellFactory = TextFieldTreeTableCell.forTreeTableColumn()
        treecMalID.setOnEditCommit { e ->
            if (e.newValue != null && e.newValue.isNotEmpty()) {
                try {
                    val number: String = e.newValue.replace("/[^0-9]+/g".toRegex(), "")
                    if (number.isNotEmpty() && e.treeTableView.getTreeItem(e.treeTablePosition.row).value is MAL.Registro) {
                        if (!ProcessaComicInfo.getById(number.toLong(), e.treeTableView.getTreeItem(e.treeTablePosition.row).value as MAL.Registro) &&
                            e.oldValue != null && e.oldValue.isNotEmpty())
                            e.treeTableView.getTreeItem(e.treeTablePosition.row).value.id = e.oldValue.toLong()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (e.oldValue != null && e.oldValue.isNotEmpty())
                        e.treeTableView.getTreeItem(e.treeTablePosition.row).value.id = e.oldValue.toLong()
                }
            } else if (e.oldValue != null && e.oldValue.isNotEmpty())
                e.treeTableView.getTreeItem(e.treeTablePosition.row).value.id = e.oldValue.toLong()
            treeTabela.requestFocus()
            treeTabela.refresh()
        }

        editaColunas()
        selecionaRegistros()
    }

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        ProcessaComicInfo.setPai(this)
        cbLinguagem.items.addAll(Language.PORTUGUESE, Language.ENGLISH, Language.JAPANESE)
        cbLinguagem.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                when (newValue) {
                    Language.PORTUGUESE -> txtDescricaoCapitulo.text = "Capítulo"
                    Language.ENGLISH -> txtDescricaoCapitulo.text = "Chapter"
                    Language.JAPANESE -> txtDescricaoCapitulo.text = "第%s話"
                    else -> {}
                }
            }
        }
        cbLinguagem.selectionModel.selectFirst()
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ESCAPE)) cbLinguagem.selectionModel.clearSelection() else if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB)
        }
        linkaCelulas()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MangasComicInfoController::class.java)
        val fxmlLocate: URL get() = MangasComicInfoController::class.java.getResource("/view/mangas/MangaComicInfo.fxml") as URL
        var selecionado = false
    }
}