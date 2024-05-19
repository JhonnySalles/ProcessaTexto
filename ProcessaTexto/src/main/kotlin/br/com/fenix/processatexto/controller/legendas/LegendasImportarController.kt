package br.com.fenix.processatexto.controller.legendas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.CheckBoxTableCellCustom
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.BaseController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.processar.ProcessarLegendas
import br.com.fenix.processatexto.service.LegendasServices
import com.jfoenix.controls.*
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import com.nativejavafx.taskbar.TaskbarProgressbar
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.jisho.textosJapones.model.entities.subtitle.Arquivo
import br.com.fenix.processatexto.model.entities.subtitle.Legenda
import br.com.fenix.processatexto.model.enums.Dicionario
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.util.constraints.Validadores
import br.com.fenix.processatexto.util.converter.IntegerConverter
import javafx.scene.control.ProgressBar
import javafx.scene.layout.StackPane
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*


class LegendasImportarController : Initializable, BaseController {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var cbLinguagemFilaSql: JFXComboBox<Language>

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var cbBase: JFXComboBox<String>

    @FXML
    private lateinit var txtPipe: JFXTextField

    @FXML
    private lateinit var txtNome: JFXTextField

    @FXML
    private lateinit var txtPrefixoSom: JFXTextField

    @FXML
    private lateinit var txtCaminho: JFXTextField

    @FXML
    private lateinit var btnCaminho: JFXButton

    @FXML
    private lateinit var btnArquivo: JFXButton

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var btnLimpar: JFXButton

    @FXML
    private lateinit var ckbVocabulario: JFXCheckBox

    @FXML
    private lateinit var ckbMarcarTodos: JFXCheckBox

    @FXML
    private lateinit var tbTabela: TableView<Arquivo>

    @FXML
    private lateinit var tcMarcado: TableColumn<Arquivo, Boolean>

    @FXML
    private lateinit var tcArquivo: TableColumn<Arquivo, String>

    @FXML
    private lateinit var tcEpisodio: TableColumn<Arquivo, Int>
    
    private var ARQUIVOS: ObservableList<Arquivo> = FXCollections.observableArrayList()
    
    override val stackPane: StackPane get() = controllerPai.stackPane

    override val root: AnchorPane get() = controllerPai.root
    override val barraProgresso: ProgressBar get() = TODO("Not yet implemented")

    override fun habilitar() {
        TODO("Not yet implemented")
    }

    private lateinit var controller: LegendasController
    var controllerPai: LegendasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    private var desativar: Boolean = false
    private val services: LegendasServices = LegendasServices()
    private val processar: ProcessarLegendas = ProcessarLegendas(this)
    
    @FXML
    private fun onBtnLimpar() = limpar()

    @FXML
    private fun onBtnMarcarTodos() {
        for (arquivo in ARQUIVOS) 
            arquivo.isProcessar = ckbMarcarTodos.isSelected
        tbTabela.refresh()
    }

    @FXML
    private fun onBtnProcessar() {
        if (btnProcessar.accessibleText.equals("PROCESSANDO", true)) {
            desativar = true
            return
        }
        if (!valida()) 
            return
        
        desabilitaBotoes()
        btnProcessar.accessibleText = "PROCESSANDO"
        btnProcessar.text = "Pausar"
        desativar = false
        
        val pipe = if (txtPipe.text == null || txtPipe.text.trim().isEmpty()) "\t" else txtPipe.text
        val nome: String = txtNome.text.trim()
        val caminho: String = txtCaminho.text.trim()
        val lingua: Language = cbLinguagem.value
        val base: String = cbBase.editor.text
        val prefix: String = txtPrefixoSom.text.trim()
        val isVocab: Boolean = ckbVocabulario.isSelected
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Legendas - Processar arquivos"
        
        val processar: Task<Void> = object : Task<Void>() {
            var i: Long = 0
            var error = false
            val dicionario = MenuPrincipalController.controller.dicionario
            val modo = MenuPrincipalController.controller.modo
            val linguagemFilaSql = cbLinguagemFilaSql.selectionModel.selectedItem
            
            @Override
            override fun call(): Void? {
                try {
                    val processados = File(caminho, ARQUIVO_FULL)
                    if (processados.exists()) 
                        processados.delete()
                    processados.createNewFile()
                    for (arquivo in ARQUIVOS) {
                        i++
                        updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size)
                        updateProgress(i, ARQUIVOS.size.toLong())
                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported()) 
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, ARQUIVOS.size.toLong(), TaskbarProgressbar.Type.NORMAL)
                        }
                        if (!arquivo.isProcessar) 
                            continue
                        
                        if (desativar) 
                            break

                        val path: String = arquivo.arquivo.parent
                        val original = File("$path\\original")
                        if (!original.exists()) original.mkdirs()
                        var backup = File(path + "\\original\\" + arquivo.arquivo.name)
                        if (!backup.exists()) {
                            updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size + " - Gerando backup")
                            Files.copy(arquivo.arquivo.toPath(), backup.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
                            backup = File(path + "\\original\\" + arquivo.arquivo.name.substring(0, arquivo.arquivo.name.lastIndexOf(".tsv")) + ".media")
                            backup.mkdirs()
                            val pastaMidia = File(arquivo.arquivo.absolutePath.substring(0, arquivo.arquivo.absolutePath.lastIndexOf(".tsv")) + ".media")

                            if (pastaMidia.exists())
                                for (midia in pastaMidia.listFiles())
                                    Files.copy(midia.toPath(), File(backup, midia.name).toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
                        }

                        val legendas: MutableList<Legenda> = mutableListOf()
                        val tmp = File(arquivo.arquivo.absolutePath + ".tmp")
                        if (tmp.exists())
                            tmp.delete()

                        tmp.createNewFile()
                        updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size + " - Processando legendas")
                        BufferedWriter(FileWriter(tmp)).use { writer ->
                            BufferedReader(FileReader(arquivo.arquivo, StandardCharsets.UTF_8)).use { reader ->
                                var seq = 0
                                var line: String
                                val renomear: MutableMap<String, String> = mutableMapOf()
                                while (reader.readLine().also { line = it } != null) {
                                    if (line.trim().isEmpty())
                                        continue

                                    seq++
                                    var idAnki = ""
                                    var tempo = ""
                                    var texto = ""
                                    var traducao = ""
                                    var som = ""
                                    var imagem = ""
                                    var vocabulario = ""
                                    val colunas = line.split(pipe)
                                    tempo = "0" + colunas[1].substring(colunas[1].lastIndexOf("_") + 1)
                                    tempo = (tempo.substring(0, 2) + ":" + tempo.substring(3, 5)) + ":" + tempo.substring(6)
                                    for (i in 2 until colunas.size) {
                                        if (colunas[i].contains("[sound:"))
                                            som = colunas[i]
                                        else if (colunas[i].contains("<img src="))
                                            imagem = colunas[i]
                                    }
                                    for (i in 2 until colunas.size) {
                                        if (colunas[i].equals(som, true) || colunas[i].equals(imagem, true))
                                            continue
                                        if (texto.isEmpty())
                                            texto = colunas[i]
                                        else {
                                            traducao = colunas[i]
                                            break
                                        }
                                    }
                                    if (isVocab && texto.isNotEmpty())
                                        vocabulario = getVocabulario(dicionario, modo, linguagemFilaSql, texto)

                                    legendas.add(Legenda(null, seq, arquivo.episodio, lingua, tempo, texto, traducao, vocabulario, som, imagem))

                                    if (som.isNotEmpty()) {
                                        val chave: String = som.replace("[sound:", "").replace("]", "")
                                        val novo = nome + " - Episódio " + String.format("%02d", arquivo.episodio) + (if (prefix.isNotEmpty()) " $prefix" else "") + " - " + tempo.replace(":", ".") + chave.substring(chave.lastIndexOf("."))
                                        renomear[chave] = novo
                                        som = "[sound:$novo]"
                                    }

                                    if (imagem.isNotEmpty()) {
                                        val chave: String = imagem.replace("<img src=\"", "").replace("\">", "")
                                        val novo = nome + " - Episódio " + String.format("%02d", arquivo.episodio) + " - " + tempo.replace(":", ".") + chave.substring(chave.lastIndexOf("."))
                                        renomear[chave] = novo
                                        imagem = "<img src=\"$novo\">"
                                    }

                                    idAnki = nome + " - Episódio " + String.format("%02d", arquivo.episodio) + " - " + tempo
                                    writer.append(idAnki + pipe + (if (som.isNotEmpty()) som + pipe else "") + (if (imagem.isNotEmpty()) imagem + pipe else "") + texto + (if (isVocab) pipe + vocabulario else "") + if (traducao.isNotEmpty()) pipe + traducao else "")
                                    writer.newLine()
                                }
                                updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size + " - Salvando legendas")
                                services.salvar(base, legendas)
                                if (isVocab) {
                                    val schema: String = services.schema
                                    val select = "SELECT id, texto FROM " + schema + "." + base + "  WHERE linguagem = " + '"' + lingua.sigla.uppercase(Locale.getDefault()) + '"' + " AND vocabulario IS NOT NULL;"
                                    val update = "UPDATE $schema.$base SET Vocabulario = ? WHERE id = ?;"
                                    var delete = "UPDATE $schema.$base SET Vocabulario = '' WHERE Vocabulario IS NOT NULL;"

                                    if (!services.existFila(delete))
                                        services.insertOrUpdateFila(FilaSQL(select, update, delete, linguagemFilaSql, isExporta = false, isLimpeza = false))

                                    delete = "UPDATE $schema.$base SET Vocabulario = NULL WHERE Vocabulario = '';"
                                    if (!services.existFila(delete))
                                        services.insertOrUpdateFila(
                                            FilaSQL("", "", delete, linguagemFilaSql,
                                            isExporta = false, isLimpeza = true)
                                        )
                                }

                                val pastaMidia = File(arquivo.arquivo.absolutePath.substring(0, arquivo.arquivo.absolutePath.lastIndexOf(".tsv")) + ".media")
                                if (pastaMidia.exists()) {
                                    updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size + " - Renomeando arquivos de midia")
                                    for (midia in pastaMidia.listFiles()) {
                                        if (renomear.containsKey(midia.name)) {
                                            if (midia.name.lowercase(Locale.getDefault()).endsWith(".mp3")) {
                                                val mp3file = Mp3File(midia)
                                                var tag: ID3v2
                                                if (mp3file.hasId3v2Tag())
                                                    tag = mp3file.id3v2Tag
                                                else {
                                                    tag = ID3v24Tag()
                                                    mp3file.id3v2Tag = tag
                                                    tag.setTitle(midia.name.substring(0, midia.name.lastIndexOf(".")))
                                                }
                                                tag.track = arquivo.episodio.toString()
                                                tag.artist = nome
                                                tag.albumArtist = nome
                                                tag.album = "Episódio " + arquivo.episodio.toString()
                                                tag.genreDescription = "Anime"
                                                val novo = File(midia.parent, renomear[midia.name])
                                                mp3file.save(novo.absolutePath)
                                                midia.delete()
                                            } else
                                                midia.renameTo(File(midia.parent, renomear[midia.name]))
                                        }
                                    }
                                }
                            }
                            writer.flush()
                        }

                        BufferedWriter(FileWriter(processados, true)).use { writer ->
                            BufferedReader(FileReader(tmp, StandardCharsets.UTF_8)).use { reader ->
                                var line: String
                                while (reader.readLine().also { line = it } != null) {
                                    if (line.trim().isEmpty())
                                        continue
                                    writer.append(line)
                                    writer.newLine()
                                }
                            }
                            writer.flush()
                        }
                        arquivo.arquivo.delete()
                        tmp.renameTo(arquivo.arquivo)
                        arquivo.isProcessar = false
                    }
                } catch (e: Exception) {
                    LOGGER.error("Erro ao processar as legendas", e)
                    error = true
                } finally {
                    if (!desativar)
                        updateMessage("Concluído....")
                    Platform.runLater {
                        btnProcessar.accessibleText = "PROCESSAR"
                        btnProcessar.text = "Processar"
                        habilitaBotoes()
                        if (!desativar && !error)
                            limpar()
                        progress.barraProgresso.progressProperty().unbind()
                        progress.log.textProperty().unbind()
                        MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                    }
                }
                return null
            }
        }

        val processa = Thread(processar)
        progress.log.textProperty().bind(processar.messageProperty())
        progress.barraProgresso.progressProperty().bind(processar.progressProperty())
        processa.start()
    }

    @FXML
    private fun onBtnCarregarArquivo() {
        txtCaminho.text = selecionaPasta(txtCaminho.text, true)
        carregaArquivos()
    }

    @FXML
    private fun onBtnCarregarPasta() {
        txtCaminho.text = selecionaPasta(txtCaminho.text, false)
        carregaArquivos()
    }

    private fun valida(): Boolean {
        var valido = true
        if (cbLinguagemFilaSql.value == null) {
            valido = false
            cbLinguagemFilaSql.unFocusColor = Color.RED
            AlertasPopup.AlertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar uma linguagem de processamento.")
        }
        if (cbBase.value == null || cbBase.value.isEmpty()) {
            valido = false
            cbBase.unFocusColor = Color.RED
            AlertasPopup.AlertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar uma base.")
        }
        if (cbLinguagem.value == null) {
            valido = false
            cbLinguagem.unFocusColor = Color.RED
            AlertasPopup.AlertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar uma linguagem.")
        }
        if (txtNome.text.isEmpty()) {
            valido = false
            txtNome.unFocusColor = Color.RED
            AlertasPopup.AlertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um nome.")
        }
        if (ARQUIVOS.isEmpty()) {
            valido = false
            AlertasPopup.AlertaModal(stackPane, root, mutableListOf(), "Alerta", "Nenhum arquivo para processar.")
        }
        return valido
    }

    private fun desabilitaBotoes() {
        btnLimpar.isDisable = true
        cbBase.isDisable = true
        cbLinguagemFilaSql.isDisable = true
        cbLinguagem.isDisable = true
        txtPipe.isDisable = true
        txtNome.isDisable = true
        txtPrefixoSom.isDisable = true
        txtCaminho.isDisable = true
        btnCaminho.isDisable = true
        btnArquivo.isDisable = true
        tbTabela.isDisable = true
        ckbMarcarTodos.isDisable = true
        ckbVocabulario.isDisable = true
    }

    private fun habilitaBotoes() {
        btnLimpar.isDisable = false
        cbBase.isDisable = false
        cbLinguagemFilaSql.isDisable = false
        cbLinguagem.isDisable = false
        txtPipe.isDisable = false
        txtNome.isDisable = false
        txtPrefixoSom.isDisable = false
        txtCaminho.isDisable = false
        btnCaminho.isDisable = false
        btnArquivo.isDisable = false
        tbTabela.isDisable = false
        ckbMarcarTodos.isDisable = false
        if (cbLinguagem.value === Language.PORTUGUESE) ckbVocabulario.isDisable = false
    }

    private fun getVocabulario(dicionario: Dicionario, modo: Modo, linguagem: Language, palavra: String): String {
        var vocabulario = ""
        when (linguagem) {
            Language.JAPANESE -> vocabulario = processar.processarJapones(dicionario, modo, palavra)
            Language.ENGLISH -> vocabulario = processar.processarIngles(palavra)
            else -> {}
        }
        return vocabulario
    }

    private fun selecionaPasta(pasta: String?, isFile: Boolean): String {
        val arquivo: File = if (isFile) {
            val chooser = FileChooser()
            chooser.initialDirectory = File(System.getProperty("user.home"))
            chooser.title = "Selecione o arquivo."
            chooser.extensionFilters.add(FileChooser.ExtensionFilter("Arquivo de texto (*.tsv)", "*.tsv"))
            if (pasta != null && pasta.isNotEmpty()) {
                val initial = File(pasta)
                if (initial.isDirectory)
                    chooser.initialDirectory = initial else chooser.initialDirectory = File(initial.path)
            }
            chooser.showOpenDialog(null)
        } else {
            val chooser = DirectoryChooser()
            chooser.initialDirectory = File(System.getProperty("user.home"))
            chooser.title = "Selecione a pasta."
            chooser.showDialog(null)
        }
        return if (arquivo.isDirectory) arquivo.absolutePath else arquivo.path
    }

    private fun getArquivo(arquivo: File): Arquivo {
        var episodio = 0
        try {
            episodio = arquivo.name.replace("\\D".toRegex(), "").trim().toInt()
        } catch (_: Exception) {
        }
        return Arquivo(arquivo.absolutePath, arquivo.name, arquivo, episodio)
    }

    private fun limpar() {
        txtCaminho.text = ""
        carregaArquivos()
    }

    private fun carregaArquivos() {
        val arquivos: MutableList<Arquivo> = mutableListOf()
        try {
            if (txtCaminho.text == null || txtCaminho.text.trim().isEmpty()) return
            val pasta = File(txtCaminho.text)
            if (!pasta.exists()) {
                txtCaminho.unFocusColor = Color.RED
                return
            }
            if (pasta.isDirectory) {
                for (arq in pasta.listFiles())
                    if (arq.name.endsWith(".tsv") && !arq.name.equals(ARQUIVO_FULL, true))
                        arquivos.add(getArquivo(arq))
            } else if (pasta.name.endsWith(".tsv") && !pasta.name.equals(ARQUIVO_FULL, true))
                arquivos.add(getArquivo(pasta))
        } finally {
            ARQUIVOS = FXCollections.observableArrayList(arquivos)
            tbTabela.setItems(ARQUIVOS)
            ckbMarcarTodos.isSelected = true
            tbTabela.refresh()
        }
    }

    private fun editaColunas() {
        tcMarcado.setCellValueFactory(PropertyValueFactory("processar"))
        tcArquivo.setCellValueFactory(PropertyValueFactory("nome"))
        tcEpisodio.setCellValueFactory(PropertyValueFactory("episodio"))
        tcEpisodio.setCellFactory(TextFieldTableCell.forTableColumn(IntegerConverter()))
        tcEpisodio.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].episodio = e.newValue
            tbTabela.requestFocus()
        }
        tcMarcado.setCellValueFactory { param ->
            val item: Arquivo = param.value
            val booleanProp = SimpleBooleanProperty(item.isProcessar)
            booleanProp.addListener { _, _, newValue ->
                item.isProcessar = newValue
                tbTabela.refresh()
            }
            booleanProp
        }
        tcMarcado.setCellFactory {
            val cell: CheckBoxTableCellCustom<Arquivo, Boolean> = CheckBoxTableCellCustom()
            cell.alignment = Pos.CENTER
            cell
        }
    }

    private fun linkaCelulas() = editaColunas()

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL, arg1: ResourceBundle) {
        linkaCelulas()
        btnProcessar.accessibleText = "PROCESSAR"
        Validadores.setComboBoxNotEmpty(cbLinguagemFilaSql, false)
        Validadores.setComboBoxNotEmpty(cbLinguagem, false)
        Validadores.setComboBoxNotEmpty(cbBase, true)
        Validadores.setTextFieldNotEmpty(txtNome)
        txtNome.focusedProperty().addListener { _, oldPropertyValue, _ ->
            if (oldPropertyValue)
                if (txtNome.text.contains("\\"))
                    txtNome.text = txtNome.text.replace("\\\\", " ")
        }
        txtCaminho.focusedProperty().addListener { _, oldPropertyValue, _ -> if (oldPropertyValue) carregaArquivos() }

        try {
            cbBase.items.setAll(services.tabelas)
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
        cbBase.focusedProperty().addListener { _, oldValue, _ ->
            if (oldValue) {
                val base: String = cbBase.editor.text
                if (base.isNotEmpty()) {
                    if (base.contains(" "))
                        cbBase.editor.text = base.replace(" ", "_").lowercase(Locale.getDefault())
                    else
                        cbBase.editor.text = base.lowercase(Locale.getDefault())
                }
            }
        }
        cbLinguagemFilaSql.items.addAll(Language.JAPANESE, Language.ENGLISH)
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ESCAPE))
                cbLinguagem.selectionModel.clearSelection()
            else if (ke.code.equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB)
        }
        cbLinguagemFilaSql.selectionModel.selectFirst()
        cbLinguagem.items.addAll(Language.ENGLISH, Language.PORTUGUESE)
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ESCAPE)) cbLinguagem.selectionModel.clearSelection() else if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB)
        }
        cbLinguagem.selectionModel.select(Language.PORTUGUESE)
        cbLinguagem.setOnAction {
            if (cbLinguagem.value !== Language.PORTUGUESE) {
                ckbVocabulario.isSelected = false
                ckbVocabulario.isDisable = true
            } else ckbVocabulario.isDisable = false
        }
        txtCaminho.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtNome.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtPrefixoSom.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LegendasImportarController::class.java)
        private const val ARQUIVO_FULL = "processados.tsv"
        val fxmlLocate: URL get() = LegendasImportarController::class.java.getResource("/view/legendas/LegendasImportar.fxml")
    }
}