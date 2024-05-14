package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTreeTableCellCustom
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaCapitulo
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVinculo
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.service.MangaServices
import br.com.fenix.processatexto.service.VincularServices
import br.com.fenix.processatexto.util.configuration.Configuracao
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.css.PseudoClass
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.stage.DirectoryChooser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.URL
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class MangasJsonController : Initializable {

    val vinculado: PseudoClass = PseudoClass.getPseudoClass("vinculado")


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
    private lateinit var txtCaminhoSalvar: JFXTextField

    @FXML
    private lateinit var btnCaminhoSalvar: JFXButton

    @FXML
    private lateinit var btnCarregar: JFXButton

    @FXML
    private lateinit var btnGerarJson: JFXButton

    @FXML
    private lateinit var ckbSepararPorCapitulos: JFXCheckBox

    @FXML
    private lateinit var ckbInverterOrdemTexto: JFXCheckBox

    @FXML
    private lateinit var ckbInserirArquivos: JFXCheckBox

    @FXML
    private lateinit var ckbExcluirAoInserirArquivos: JFXCheckBox

    @FXML
    private lateinit var ckbCarregaVinculos: JFXCheckBox

    @FXML
    private lateinit var ckbApenasVinculos: JFXCheckBox

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


    private var serviceManga: MangaServices = MangaServices()
    private val serviceVinculo: VincularServices = VincularServices()
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
    private fun onBtnGerarJson() {
        if (txtCaminhoSalvar.text.isEmpty()) {
            AlertasPopup.AvisoModal("Aviso", "Necessário informar um caminho de destino.")
            return
        }
        if (ckbInserirArquivos.isSelected && Configuracao.caminhoWinrar.isEmpty()) {
            AlertasPopup.AvisoModal("Aviso", "Necessário informar o caminho do winrar nas configurações.")
            return
        }
        if (btnGerarJson.accessibleText.equals("GERANDO", false)) {
            PAUSAR = true
            return
        }

        btnGerarJson.accessibleText = "GERANDO"
        btnGerarJson.text = "Pausar"
        btnCarregar.isDisable = true
        treeBases.isDisable = true
        gerar()
    }

    @FXML
    private fun onBtnCarregarCaminhoSalvar() {
        val caminho = selecionaPasta(txtCaminhoSalvar.text)
        txtCaminhoSalvar.text = caminho
    }

    private fun selecionaPasta(pasta: String?): String {
        val fileChooser = DirectoryChooser()
        fileChooser.title = "Selecione a pasta de destino"
        if (pasta != null && pasta.isNotEmpty())
            fileChooser.initialDirectory = File(pasta)

        val caminho: File = fileChooser.showDialog(null)
        return if (caminho == null) "" else caminho.absolutePath
    }

    @FXML
    private fun onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.root, ckbMarcarTodos.isSelected)
        treeBases.refresh()
    }

    fun habilitar() {
        treeBases.isDisable = false
        btnGerarJson.accessibleText = "GERAR"
        btnGerarJson.text = "Gerar Json"
        btnCarregar.isDisable = false
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
    }

    private val patern = ".*\\.(zip|cbz|rar|cbr|tar)$".toRegex()
    private fun procuraArquivo(caminho: String?, arquivo: String?): File? {
        if (arquivo == null || arquivo.isEmpty())
            return null

        var encontrado: File? = null
        val pasta = File(caminho)
        for (item in pasta.listFiles()) {
            if (item.isDirectory)
                encontrado = procuraArquivo(item.absolutePath, arquivo)

            if (encontrado != null)
                return encontrado

            if (item.name.lowercase(Locale.getDefault()).contains(".json"))
                continue

            if (!item.name.lowercase(Locale.getDefault()).matches(patern))
                continue

            if (item.name.lowercase(Locale.getDefault()).contains(arquivo))
                return item
        }
        return encontrado
    }

    private fun procuraArquivo(caminho: String?, nome: String?, volume: Int?): File? {
        if (nome == null || volume == null)
            return null

        var encontrado: File? = null
        val pasta = File(caminho)
        for (item in pasta.listFiles()) {
            if (item.isDirectory)
                encontrado = procuraArquivo(item.absolutePath, nome, volume)

            if (encontrado != null)
                return encontrado

            if (item.name.lowercase(Locale.getDefault()).contains(".json"))
                continue

            if (!item.name.lowercase(Locale.getDefault()).matches(patern))
                continue

            if (item.name.lowercase(Locale.getDefault()).contains("(jap)") || item.name.lowercase(Locale.getDefault()).contains("(jpn)")) {
                if (item.name.lowercase(Locale.getDefault()).contains(nome) && (item.name.lowercase(Locale.getDefault())
                        .contains("volume " + String.format("%02d", volume) + " ")
                            || item.name.lowercase(Locale.getDefault()).contains("volume " + String.format("%03d", volume) + " "))
                ) {
                    encontrado = item
                    break
                }
            } else if (item.name.lowercase(Locale.getDefault()).contains(nome) && (item.name.lowercase(Locale.getDefault())
                    .contains("volume " + String.format("%02d", volume))
                        || item.name.lowercase(Locale.getDefault()).contains("volume " + String.format("%03d", volume)))
            ) {
                encontrado = item
                break
            }
        }
        return encontrado
    }

    private fun insereDentroArquivos(localPasta: String?, nomeArquivo: String, nome: String, volume: Int, localJson: String) {
        var arquivo: File? = procuraArquivo(localPasta, nomeArquivo)
        if (arquivo == null)
            arquivo = procuraArquivo(localPasta, nome, volume)

        // Necessário adicionar o winrar no path do windows.
        if (arquivo != null) {
            val json = File(localJson)
            val comando = ("cmd.exe /C cd \"" + winrar + "\" &&rar a -ep " + '"' + arquivo.path + '"' + " " + '"'
                    + json.path + '"')
            println("cmd.exe /C cd \"$winrar\"")
            println("rar a -ep " + '"' + arquivo.path + '"' + " " + '"' + json.path + '"')
            try {
                val rt: Runtime = Runtime.getRuntime()
                val proc: Process = rt.exec(comando)
                println("Resultado: " + proc.waitFor())
                var resultado = ""
                val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
                var s: String? = null
                while (stdInput.readLine().also { s = it } != null)
                    resultado += "$s"

                if (resultado.isNotEmpty())
                    println("Output comand:\n$resultado")
                s = null
                resultado = ""
                val stdError = BufferedReader(InputStreamReader(proc.errorStream))
                while (stdError.readLine().also { s = it } != null)
                    resultado += "$s".trimIndent()
                if (resultado.isNotEmpty()) {
                    println("Error comand: $resultado Necessário adicionar o rar no path e reiniciar a aplicação.")
                } else if (excluirAoInserir)
                    json.delete()
            } catch (e: Exception) {
                println(e)
                LOGGER.error(e.message, e)
            }
        }
    }

    private var isSepararCapitulo: Boolean = false
    private var I: Long = 0
    private var error: String = ""
    private var destino: String = ""
    private var inserirArquivos: Boolean = false
    private var excluirAoInserir: Boolean = false
    private var winrar: String = ""

    private fun gerar() {
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        isSepararCapitulo = ckbSepararPorCapitulos.isSelected
        destino = txtCaminhoSalvar.text
        PAUSAR = false

        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

        progress!!.titulo.text = "Json"
        val gerarJson: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    inserirArquivos = ckbInserirArquivos.isSelected
                    excluirAoInserir = ckbExcluirAoInserirArquivos.isSelected
                    winrar = Configuracao.caminhoWinrar
                    error = ""
                    updateMessage("Gravando Jsons....")
                    val removeLinguagemCapitulo: ExclusionStrategy = object : ExclusionStrategy {
                        @Override
                        override fun shouldSkipField(field: FieldAttributes): Boolean {
                            if (field.declaringClass === MangaCapitulo::class.java && field.name.equals("lingua"))
                                return true

                            return if (field.declaringClass === MangaCapitulo::class.java && field.name.equals("manga"))
                                true
                            else
                                field.declaringClass === MangaCapitulo::class.java && field.name.equals("volume")
                        }

                        @Override
                        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                            return false
                        }
                    }
                    var gson: Gson? = null
                    val formatter: DecimalFormat = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
                    formatter.applyPattern("000.###")
                    gson = if (isSepararCapitulo)
                        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    else
                        GsonBuilder().addSerializationExclusionStrategy(removeLinguagemCapitulo).excludeFieldsWithoutExposeAnnotation().create()

                    for ((I, tabela) in TABELAS.withIndex()) {
                        if (!tabela.isProcessar) {
                            Platform.runLater {
                                if (TaskbarProgressbar.isSupported())
                                    TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), TABELAS.size.toLong(), Type.NORMAL)
                            }
                            continue
                        }
                        for (volume in tabela.volumes) {
                            if (!volume.isProcessar)
                                continue
                            if (isSepararCapitulo) {
                                for (capitulo in volume.capitulos) {
                                    if (!capitulo.isProcessar)
                                        continue

                                    val arquivo =
                                        (destino + '/' + volume.lingua + " - " + volume.manga + " - Volume " + String.format("%03d", volume.volume) + " Capitulo "
                                                + formatter.format(capitulo.capitulo)) + ".json"
                                    val file = FileWriter(arquivo)
                                    gson.toJson(capitulo, file)
                                    file.flush()
                                    file.close()
                                    if (inserirArquivos)
                                        insereDentroArquivos(
                                            destino,
                                            volume.arquivo.lowercase(Locale.getDefault()),
                                            volume.manga.lowercase(Locale.getDefault()),
                                            volume.volume,
                                            arquivo
                                        )

                                    if (PAUSAR)
                                        break
                                }
                            } else {
                                val arquivo = destino + '/' + volume.lingua + " - " + volume.manga + " - Volume " + String.format("%03d", volume.volume) + ".json"
                                val file = FileWriter(arquivo)
                                gson.toJson(volume, file)
                                file.flush()
                                file.close()
                                if (inserirArquivos) insereDentroArquivos(
                                    destino, volume.arquivo.lowercase(Locale.getDefault()),
                                    volume.manga.lowercase(Locale.getDefault()), volume.volume, arquivo
                                )
                            }
                        }
                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), TABELAS.size.toLong(), Type.NORMAL)
                        }
                        if (PAUSAR) break
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
                    else
                        AlertasPopup.AvisoModal("Aviso", "Jsons gerado com sucesso.")
                    habilitar()
                }
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread gerar json: " + super.getMessage())
                println("Erro na thread gerar json: " + super.getMessage())
                habilitar()
            }
        }
        progress.barraProgresso.progressProperty().bind(gerarJson.progressProperty())
        progress.log.textProperty().bind(gerarJson.messageProperty())
        val t = Thread(gerarJson)
        t.start()
    }

    private var BASE: String? = null
    private var MANGA: String? = null
    private var VOLUME: Int? = null
    private var CAPITULO: Float? = null
    private var LINGUAGEM: Language? = null
    private var DADOS: TreeItem<Manga>? = null
    private fun carregar() {
        MenuPrincipalController.controller.getLblLog().text = "Carregando json..."
        if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())
        btnCarregar.isDisable = true
        btnGerarJson.isDisable = true
        treeBases.isDisable = true
        BASE = if (cbBase.selectionModel.selectedItem == null) "" else cbBase.selectionModel.selectedItem.trim()
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
                    serviceManga = MangaServices()
                    if (ckbCarregaVinculos.isSelected && ckbApenasVinculos.isSelected) {
                        TABELAS = FXCollections.observableArrayList(serviceVinculo.selectTabelasJson(BASE!!, MANGA!!, VOLUME!!, CAPITULO!!, LINGUAGEM!!))
                    } else {
                        TABELAS = FXCollections.observableArrayList(
                            serviceManga.selectTabelasJson(
                                BASE!!,
                                MANGA!!,
                                VOLUME!!,
                                CAPITULO!!,
                                LINGUAGEM!!,
                                ckbInverterOrdemTexto.isSelected
                            )
                        )
                        if (ckbCarregaVinculos.isSelected) {
                            for (tabela in TABELAS)
                                tabela.vinculados = serviceVinculo.getMangaVinculo(tabela.base, MANGA!!, VOLUME!!, CAPITULO!!, LINGUAGEM!!)
                        }
                    }
                    DADOS = treeData
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.succeeded()
                Platform.runLater {
                    treeBases.setRoot(DADOS)
                    MenuPrincipalController.controller.getLblLog().text = ""
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    ckbMarcarTodos.isSelected = true
                    btnCarregar.isDisable = false
                    btnGerarJson.isDisable = false
                    treeBases.setDisable(false)
                }
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de carregamento de itens: " + super.getMessage())
                println("Erro na thread de carregamento de itens: " + super.getMessage())
                habilitar()
            }
        }
        val t = Thread(carregaItens)
        t.start()
    }

    private fun getLinguaVinculo(vinculo: MangaVinculo): String {
        var lingua: String = vinculo.manga!!.lingua.sigla.uppercase(Locale.getDefault()) + " | "
        for (vi in vinculo.vinculos)
            lingua += vi.lingua.sigla.uppercase(Locale.getDefault()) + " -- "

        lingua = lingua.substring(0, lingua.lastIndexOf(" -- "))
        return lingua
    }// Implementa um nivel por tipo

    // ---------------- Adicionado na tabela ---------------- //
// Implementa um nivel por tipo

    // ---------------- Vinculados ---------------- //
    // ---------------- Mangas ---------------- //
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

                // ---------------- Mangas ---------------- //
                for (volume in tabela.volumes) {

                    // Implementa um nivel por tipo
                    if (!volume.manga.equals(volumeAnterior, true) || itmManga == null) {
                        volumeAnterior = volume.manga
                        itmManga = TreeItem<Manga>(Manga(tabela.base, volume.manga, "..."))
                        itmTabela.children.add(itmManga)
                        itmTabela.isExpanded = true
                        itmLingua = TreeItem<Manga>(
                            Manga(
                                tabela.base, volume.manga,
                                volume.lingua.sigla.uppercase(Locale.getDefault())
                            )
                        )
                        linguagemAnterior = volume.lingua
                        itmManga.children.add(itmLingua)
                    }
                    if (linguagemAnterior == null || volume.lingua.compareTo(linguagemAnterior) !== 0) {
                        itmLingua = TreeItem<Manga>(
                            Manga(
                                tabela.base, volume.manga,
                                volume.lingua.sigla.uppercase(Locale.getDefault())
                            )
                        )
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
                var lingua = ""
                var linguaAnterior = ""

                // ---------------- Vinculados ---------------- //
                for (vinculo in tabela.vinculados) {
                    linguagemAnterior = null
                    val volume: MangaVolume = vinculo.manga!!
                    volume.isVinculo = true
                    lingua = getLinguaVinculo(vinculo)

                    // Implementa um nivel por tipo
                    if (!volume.manga.equals(volumeAnterior, true) || itmManga == null) {
                        volumeAnterior = volume.manga
                        itmManga = TreeItem<Manga>(Manga(tabela.base, volume.manga, "...", true))
                        itmTabela.children.add(itmManga)
                        itmTabela.isExpanded = true
                        itmLingua = TreeItem<Manga>(Manga(tabela.base, volume.manga, lingua, true))
                        linguagemAnterior = volume.lingua
                        linguaAnterior = getLinguaVinculo(vinculo)
                        itmManga.children.add(itmLingua)
                    }
                    if (linguagemAnterior == null || volume.lingua.compareTo(linguagemAnterior) !== 0) {
                        itmLingua = TreeItem<Manga>(Manga(tabela.base, volume.manga, lingua, true))
                        linguagemAnterior = volume.lingua
                        linguaAnterior = getLinguaVinculo(vinculo)
                        itmManga.children.add(itmLingua)
                    }
                    volume.linguagem = linguaAnterior
                    volume.base = tabela.base
                    val itmVolume: TreeItem<Manga> = TreeItem<Manga>(volume)
                    for (capitulo in volume.capitulos) {
                        capitulo.base = tabela.base
                        capitulo.nomePagina = "..."
                        capitulo.linguagem = linguaAnterior
                        capitulo.isVinculo = true
                        itmVolume.children.add(TreeItem(capitulo))
                    }
                    itmLingua!!.children.add(itmVolume)
                }

                // ---------------- Adicionado na tabela ---------------- //
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
            val cell = CheckBoxTreeTableCellCustom<Manga, Boolean>()
            cell.alignment = Pos.CENTER
            cell
        }
        treeBases.setRowFactory {
            object : TreeTableRow<Manga>() {
                @Override
                override fun updateItem(item: Manga?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null) {
                        style = ""
                        pseudoClassStateChanged(vinculado, false)
                    } else
                        pseudoClassStateChanged(vinculado, item.isVinculo)
                }
            }
        }
    }

    private fun linkaCelulas() {
        treecMacado.setCellValueFactory(TreeItemPropertyValueFactory("processar"))
        treecBase.setCellValueFactory(TreeItemPropertyValueFactory("base"))
        treecManga.setCellValueFactory(TreeItemPropertyValueFactory("manga"))
        treecLinguagem.setCellValueFactory(TreeItemPropertyValueFactory("linguagem"))
        treecVolume.setCellValueFactory(TreeItemPropertyValueFactory("volume"))
        treecCapitulo.setCellValueFactory(TreeItemPropertyValueFactory("capitulo"))
        treeBases.isShowRoot = false
        editaColunas()
    }

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL, arg1: ResourceBundle) {
        try {
            cbBase.items.setAll(serviceManga.tabelas)
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
        cbBase.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue.isNotEmpty()) {
                if (cbBase.items.isEmpty())
                    cbBase.setUnFocusColor(Color.RED)
                else
                    cbBase.setUnFocusColor(Color.web("#106ebe"))
            }
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
        cbLinguagem.items.addAll(Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE, Language.PORTUGUESE_GOOGLE)
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ESCAPE))
                cbLinguagem.selectionModel.clearSelection()
            else if (ke.code.equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB)
        }
        txtManga.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnVolume.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        spnCapitulo.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtCaminhoSalvar.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        ckbApenasVinculos.selectedProperty().addListener { _, _, newVal -> if (newVal) ckbCarregaVinculos.selectedProperty().set(true) }
        ckbCarregaVinculos.selectedProperty().addListener { _, _, newVal -> if (!newVal) ckbApenasVinculos.selectedProperty().set(false) }
        spnVolume.setValueFactory(SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0))
        spnCapitulo.setValueFactory(SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 99999.0, 0.0, 1.0))
        linkaCelulas()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MangasJsonController::class.java)
        val fxmlLocate: URL get() = MangasJsonController::class.java.getResource("/view/mangas/MangaJson.fxml")
    }
}