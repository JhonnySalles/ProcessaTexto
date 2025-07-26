package br.com.fenix.processatexto.controller.legendas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.BaseController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.processatexto.Processar
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.enums.Dicionario
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.processar.ProcessarLegendas
import br.com.fenix.processatexto.service.LegendasServices
import br.com.fenix.processatexto.service.VocabularioInglesServices
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import br.com.fenix.processatexto.util.configuration.Configuracao
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ProgressBar
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.robot.Robot
import javafx.stage.FileChooser
import javafx.util.StringConverter
import org.controlsfx.control.CheckComboBox
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors


class LegendasVocabularioController : Initializable, BaseController {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var txtCaminhoExportar: JFXTextField

    @FXML
    private lateinit var btnCaminhoExportar: JFXButton

    @FXML
    private lateinit var txtPipe: JFXTextField

    @FXML
    private lateinit var btnExclusao: JFXButton

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var btnProcessarTudo: JFXButton

    @FXML
    private lateinit var cbNome: JFXComboBox<String>

    @FXML
    private lateinit var btnSalvarFila: JFXButton

    @FXML
    private lateinit var ccFiltrar: CheckComboBox<String>

    @FXML
    private lateinit var btnExportarFila: JFXButton

    @FXML
    private lateinit var btnExecutarFila: JFXButton

    @FXML
    private lateinit var txtAreaSelect: JFXTextArea

    @FXML
    private lateinit var txtAreaUpdate: JFXTextArea

    @FXML
    private lateinit var txtAreaDelete: JFXTextArea

    @FXML
    private lateinit var cbExporta: JFXCheckBox

    @FXML
    private lateinit var cbLimpeza: JFXCheckBox

    @FXML
    private lateinit var txtAreaVocabulario: JFXTextArea

    @FXML
    private lateinit var tbLista: TableView<Processar>

    @FXML
    private lateinit var tcId: TableColumn<Processar, String>

    @FXML
    private lateinit var tcOriginal: TableColumn<Processar, String>

    @FXML
    private lateinit var tcVocabulario: TableColumn<Processar, String>

    private val service = LegendasServices()
    private val vocabularioJapones = VocabularioJaponesServices()
    private val vocabularioIngles = VocabularioInglesServices()
    private val processar = ProcessarLegendas(this)

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

    private fun desabilitaBotoes() {
        ccFiltrar.isDisable = true
        cbNome.isDisable = true
        btnProcessar.isDisable = true
        btnExclusao.isDisable = true
        btnSalvarFila.isDisable = true
        btnExportarFila.isDisable = true
        cbExporta.isDisable = true
        cbLimpeza.isDisable = true
    }

    private fun habilitaBotoes() {
        ccFiltrar.isDisable = false
        cbNome.isDisable = false
        btnProcessar.isDisable = false
        btnExclusao.isDisable = false
        btnSalvarFila.isDisable = false
        btnExportarFila.isDisable = false
        cbExporta.isDisable = false
        cbLimpeza.isDisable = false
    }

    private fun selecionaPasta(pasta: String): String {
        val chooser = FileChooser()
        chooser.title = "Selecione a pasta de destino ou o arquivo"
        if (pasta.isNotEmpty()) {
            val initial = File(pasta)
            if (initial.isDirectory)
                chooser.initialDirectory = initial
            else
                chooser.initialDirectory = File(initial.path)
        }
        val arquivo: File? = chooser.showOpenDialog(null)
        return if (arquivo == null)
            ""
        else if (arquivo.isDirectory)
            arquivo.absolutePath + "\\arquivo.csv"
        else
            arquivo.absolutePath
    }

    @FXML
    private fun onBtnCarregarCaminhoExportar() {
        val caminho = selecionaPasta(txtCaminhoExportar.text)
        txtCaminhoExportar.text = caminho
    }

    @FXML
    private fun onBtnSalvar() {
        if (txtAreaUpdate.text.trim().isEmpty() || txtAreaUpdate.text.trim().equals("UPDATE tabela SET campo3 = ? WHERE id = ?", true)) {
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um update e um delete para prosseguir com o salvamento.")
            return
        }

        try {
            MenuPrincipalController.oController.getLblLog().text = "[LEGENDAS] Salvando as informações..."
            desabilitaBotoes()
            btnProcessarTudo.isDisable = true
            if (txtAreaDelete.text.isNotEmpty() && !txtAreaDelete.text.equals("UPDATE tabela SET campo3 = '' WHERE campo3 IS NOT NULL", true))
                service.comandoDelete(txtAreaDelete.text)
            val update: List<Processar> = tbLista.items.stream()
                .filter { revisar -> revisar.vocabulario.trim().isNotEmpty() }
                .collect(Collectors.toList())
            service.comandoUpdate(txtAreaUpdate.text, update)
            AlertasPopup.avisoModal(stackPane, root, mutableListOf(), "Salvo", "Salvo com sucesso.")
            onBtnAtualizar()
        } catch (e: SQLException) {
            oLog.error(e.message, e)
            AlertasPopup.erroModal(stackPane, root, mutableListOf(), "Erro", "Erro ao salvar as atualizações.")
        } finally {
            MenuPrincipalController.oController.getLblLog().text = ""
            habilitaBotoes()
            btnProcessarTudo.isDisable = false
        }
    }

    @FXML
    private fun onBtnAtualizar() {
        if (txtAreaSelect.text.trim().isEmpty() || txtAreaSelect.text.trim().equals("SELECT campo1 AS ID, campo2 AS ORIGINAL FROM tabela", true)) {
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um select para prosseguir com o salvamento.")
            return
        }

        try {
            MenuPrincipalController.oController.getLblLog().text = "[LEGENDAS] Atualizando...."
            tbLista.setItems(FXCollections.observableArrayList(service.comandoSelect(txtAreaSelect.text)))
            MenuPrincipalController.oController.getLblLog().text = "[LEGENDAS] Concluido...."
        } catch (e: SQLException) {
            oLog.error(e.message, e)
            AlertasPopup.erroModal(stackPane, root, mutableListOf(), "Erro", "Erro ao realizar a pesquisa.")
        }
    }

    @FXML
    private fun onBtnDeletar() {
        if (txtAreaDelete.text.trim().isEmpty() || txtAreaDelete.text.trim().equals("UPDATE tabela SET campo3 = '' WHERE campo3 IS NOT NULL", true)) {
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um delete para prosseguir com a limpeza.")
            return
        }

        try {
            MenuPrincipalController.oController.getLblLog().text = "[LEGENDAS] Iniciando o delete...."
            service.comandoDelete(txtAreaDelete.text)
        } catch (e: SQLException) {
            oLog.error(e.message, e)
            AlertasPopup.erroModal(stackPane, root, mutableListOf(), "Erro", "Erro ao salvar as atualizações.")
        } finally {
            MenuPrincipalController.oController.getLblLog().text = "[LEGENDAS] Delete do vocabulario concluido."
        }
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

    @FXML
    private fun onBtnProcessar() {
        if (tbLista.items.isEmpty() || tbLista.selectionModel.selectedItem == null)
            return

        if (tbLista.selectionModel.selectedItem.vocabulario.isEmpty()) {
            processar.vocabulario.clear()
            var linguagem: Language = Language.JAPANESE
            if (cbLinguagem.selectionModel.selectedItem == null || cbLinguagem.selectionModel.selectedItem.equals(Language.TODOS))
                cbLinguagem.selectionModel.select(Language.JAPANESE) else linguagem = cbLinguagem.selectionModel.selectedItem
            tbLista.selectionModel.selectedItem.vocabulario = getVocabulario(
                MenuPrincipalController.oController.dicionario,
                MenuPrincipalController.oController.modo, linguagem,
                tbLista.selectionModel.selectedItem.original
            )

            txtAreaVocabulario.text = processar.vocabulario.stream().collect(Collectors.joining("\n"))
        }
        tbLista.refresh()
    }

    @FXML
    private fun onBtnProcessarTudo() {
        if (btnProcessarTudo.accessibleText.equals("PROCESSANDO", true)) {
            desativar = true
            return
        }

        if (tbLista.items.isEmpty())
            return

        desabilitaBotoes()
        btnExecutarFila.isDisable = true
        tbLista.isDisable = true
        btnProcessarTudo.accessibleText = "PROCESSANDO"
        btnProcessarTudo.text = "Pausar"
        txtAreaVocabulario.text = ""
        desativar = false
        processar.vocabulario.clear()
        val progress = MenuPrincipalController.oController.criaBarraProgresso()
        progress!!.titulo.text = "Legendas - Processar Vocabulario"

        val processarTudo: Task<Void> = object : Task<Void>() {

            var lista: MutableList<Processar> = mutableListOf()
            val dicionario = MenuPrincipalController.oController.dicionario
            val modo = MenuPrincipalController.oController.modo
            var i: Long = 0

            @Override
            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                lista = tbLista.items.toMutableList()
                try {
                    var linguagem: Language = Language.JAPANESE
                    if (cbLinguagem.selectionModel.selectedItem == null || cbLinguagem.selectionModel.selectedItem.equals(Language.TODOS))
                        cbLinguagem.selectionModel.select(Language.JAPANESE)
                    else
                        linguagem = cbLinguagem.selectionModel.selectedItem

                    for (item in lista) {
                        i++
                        updateMessage("Processando item " + i + " de " + lista.size)
                        updateProgress(i, lista.size.toLong())
                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size.toLong(), Type.NORMAL)
                        }
                        if (item.vocabulario.isEmpty())
                            item.vocabulario = getVocabulario(dicionario, modo, linguagem, item.original)
                        if (desativar)
                            break
                    }
                } catch (e: Exception) {
                    oLog.error(e.message, e)
                } finally {
                    Platform.runLater { tbLista.setItems(FXCollections.observableArrayList(lista)) }
                    Platform.runLater {
                        btnProcessarTudo.accessibleText = "PROCESSAR"
                        btnProcessarTudo.text = "Processar tudo"
                        habilitaBotoes()
                        btnExecutarFila.isDisable = false
                        tbLista.isDisable = false
                        progress.barraProgresso.progressProperty().unbind()
                        progress.log.textProperty().unbind()
                        MenuPrincipalController.oController.destroiBarraProgresso(progress, "")
                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                        txtAreaVocabulario.text = processar.vocabulario.stream().collect(Collectors.joining("\n"))
                        tbLista.refresh()
                    }
                }
                return null
            }
        }
        val processa = Thread(processarTudo)
        progress.log.textProperty().bind(processarTudo.messageProperty())
        progress.barraProgresso.progressProperty().bind(processarTudo.progressProperty())
        processa.start()
    }

    @FXML
    private fun onBtnEclusao() {
        if (txtAreaVocabulario.text.isEmpty()) return
        try {
            if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
                vocabularioIngles.insertExclusao(txtAreaVocabulario.text.split("\n"))
            else
                vocabularioJapones.insertExclusao(txtAreaVocabulario.text.split("\n"))
            txtAreaVocabulario.text = ""
        } catch (e: SQLException) {
            oLog.error(e.message, e)
            AlertasPopup.erroModal(stackPane, root, mutableListOf(), "Erro", "Erro ao salvar a exclusao.")
        }
    }

    @FXML
    private fun onBtnSalvarFila() {
        if (cbLimpeza.isSelected || cbExporta.isSelected) {
            if (cbLimpeza.isSelected && txtAreaDelete.text.trim().isEmpty()) {
                AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um delete para a limpeza.")
                return
            }

            if (cbExporta.isSelected && txtAreaSelect.text.trim().isEmpty()) {
                AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um select para a exportação.")
                return
            }
        } else if (txtAreaSelect.text.trim().isEmpty() || txtAreaSelect.text.trim().equals("SELECT campo1 AS ID, campo2 AS ORIGINAL FROM tabela", true)
            || txtAreaUpdate.text.trim().isEmpty() || txtAreaUpdate.text.trim().equals("UPDATE tabela SET campo3 = ? WHERE id = ?", true)
            || txtAreaDelete.text.trim().isEmpty() || txtAreaDelete.text.trim().equals("UPDATE tabela SET campo3 = '' WHERE campo3 IS NOT NULL", true)
        ) {
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um select, update e delete para gravar na lista.")
            return
        } else if (cbLinguagem.selectionModel.selectedItem == null || cbLinguagem.selectionModel.selectedItem.equals(Language.TODOS)) {
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar uma linguagem para gravar na lista.")
            return
        }

        try {
            service.insertOrUpdateFila(
                FilaSQL(
                    cbNome.editor.text.trim().lowercase(),
                    txtAreaSelect.text.trim(),
                    txtAreaUpdate.text.trim(),
                    txtAreaDelete.text.trim(),
                    cbLinguagem.selectionModel.selectedItem,
                    cbExporta.isSelected,
                    cbLimpeza.isSelected
                )
            )
            carregaFiltros()
            AlertasPopup.avisoModal(stackPane, root, mutableListOf(), "Salvo", "Salvo com sucesso.")
        } catch (e: SQLException) {
            oLog.error(e.message, e)
            AlertasPopup.erroModal(stackPane, root, mutableListOf(), "Erro", "Erro ao salvar ao salvar a fila.")
        }
    }

    private fun validaProcessarFila(): Boolean {
        var valido = true
        if (txtPipe.text.isEmpty()) {
            valido = false
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um pipe para salvar o arquivo de exportação.")
        }
        if (txtCaminhoExportar.text.isNotEmpty()) {
            val arquivo = File(txtCaminhoExportar.text)
            if (arquivo.isDirectory) {
                valido = false
                AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um arquivo.")
            } else if (!File(arquivo.parent).canWrite()) {
                valido = false
                AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Não é possível gravar no local informado.")
            }
        }
        return valido
    }

    @FXML
    private fun onBtnProcessarFila() {
        if (btnExecutarFila.accessibleText.equals("PROCESSANDO", true) || btnExportarFila.accessibleText.equals("PROCESSANDO", true)) {
            desativar = true
            return
        }

        if (!validaProcessarFila())
            return

        desabilitaBotoes()
        btnProcessarTudo.isDisable = true
        btnExecutarFila.accessibleText = "PROCESSANDO"
        btnExecutarFila.text = "Pausar"
        desativar = false
        tbLista.items.clear()
        txtAreaSelect.isDisable = false
        txtAreaUpdate.isDisable = false
        txtAreaDelete.isDisable = false
        val progress = MenuPrincipalController.oController.criaBarraProgresso()
        progress!!.titulo.text = "Legendas - Processar Fila"
        val processarFila: Task<Void> = object : Task<Void>() {

            var lista: MutableList<Processar> = mutableListOf()
            var fila: MutableList<FilaSQL> = mutableListOf()
            val dicionario = MenuPrincipalController.oController.dicionario
            val modo = MenuPrincipalController.oController.modo
            var i: Long = 0
            var x: Int = 0
            val pipe: String = txtPipe.text
            val arquivo: File? = if (txtCaminhoExportar.text.isEmpty()) null else File(txtCaminhoExportar.text)
            var linguegem: Language? = null

            @Override
            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                try {
                    fila = service.selectFila()
                    linguegem = cbLinguagem.selectionModel.selectedItem
                    if (linguegem != null && linguegem!! != Language.TODOS)
                        fila = fila.stream().filter { f -> f.linguagem == linguegem }.collect(Collectors.toList())

                    if (ccFiltrar.checkModel.checkedItems.isNotEmpty())
                        fila = fila.stream().filter { f -> ccFiltrar.checkModel.checkedItems.contains(f.nome) }.collect(Collectors.toList())

                    val temp: List<FilaSQL> = fila.stream().filter { f -> !f.isLimpeza && !f.isExporta }.collect(Collectors.toList())
                    for (select in temp) {
                        x++
                        val lang: Language = select.linguagem
                        Platform.runLater {
                            cbNome.editor.text = select.nome
                            txtAreaSelect.text = select.select
                            txtAreaUpdate.text = select.update
                            txtAreaDelete.text = select.delete
                            txtAreaVocabulario.text = select.vocabulario
                            cbLinguagem.selectionModel.select(lang)
                            cbExporta.isSelected = select.isExporta
                            cbLimpeza.isSelected = select.isLimpeza
                        }
                        processar.clearVocabulary()
                        try {
                            updateMessage("Limpando....")
                            service.comandoDelete(select.delete)
                            updateMessage("Pesquisando....")
                            lista = service.comandoSelect(select.select)
                            i = 0
                            for (item in lista) {
                                i++
                                updateMessage("Processando fila " + x + " de " + temp.size + " - Processando item " + i + " de " + lista.size)
                                updateProgress(i, lista.size.toLong())
                                Platform.runLater {
                                    if (TaskbarProgressbar.isSupported())
                                        TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size.toLong(), Type.NORMAL)
                                }
                                item.vocabulario = getVocabulario(dicionario, modo, lang, item.original)
                                if (desativar)
                                    break
                            }
                            if (desativar)
                                break
                            updateMessage("Salvando....")
                            service.comandoUpdate(select.update, lista)
                            select.vocabulario = processar.vocabulario.stream().collect(Collectors.joining("\n"))
                            service.insertOrUpdateFila(select)
                        } catch (e: SQLException) {
                            oLog.error(e.message, e)
                        }
                    }

                    if (arquivo != null && !desativar) {
                        var select: List<FilaSQL> = fila.stream().filter(FilaSQL::isExporta)
                            .filter { f -> f.linguagem == Language.JAPANESE }
                            .collect(Collectors.toList())
                        if (select.isNotEmpty()) {
                            val file =
                                File(arquivo.parent, arquivo.name.substring(0, arquivo.name.lastIndexOf(".")) + " Japones" + arquivo.name.substring(arquivo.name.lastIndexOf(".")))
                            updateMessage("Exportando arquivo " + file.name)
                            lista = mutableListOf()
                            for (item in select)
                                lista.addAll(service.comandoSelect(item.select))
                            exportar(pipe, lista, file)
                        }
                        select = fila.stream().filter(FilaSQL::isExporta).filter { f -> f.linguagem == Language.ENGLISH }.collect(Collectors.toList())
                        if (select.isNotEmpty()) {
                            val file = File(
                                arquivo.parent,
                                arquivo.name.substring(0, arquivo.name.lastIndexOf(".")) + " Ingles" + arquivo.name.substring(arquivo.name.lastIndexOf("."))
                            )
                            updateMessage("Exportando arquivo " + file.name)
                            lista = mutableListOf()
                            for (item in select)
                                lista.addAll(service.comandoSelect(item.select))
                            exportar(pipe, lista, file)
                        }
                        updateMessage("Executando a limpeza.")
                        for (item in fila.stream().filter(FilaSQL::isLimpeza).collect(Collectors.toList()))
                            service.comandoDelete(item.delete)

                        Configuracao.caminhoSalvoArquivo = txtCaminhoExportar.text
                    }
                } catch (e: Exception) {
                    oLog.error("Erro ao processar a fila", e)
                } finally {
                    if (!desativar)
                        updateMessage("Concluído....")
                    Platform.runLater {
                        btnExecutarFila.accessibleText = "PROCESSAR"
                        btnExecutarFila.text = "Executar fila"
                        habilitaBotoes()
                        btnProcessarTudo.isDisable = false
                        tbLista.isDisable = false
                        progress.barraProgresso.progressProperty().unbind()
                        progress.log.textProperty().unbind()
                        MenuPrincipalController.oController.destroiBarraProgresso(progress, "")
                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                        cbNome.editor.text = ""
                        txtAreaSelect.text = ""
                        txtAreaUpdate.text = ""
                        txtAreaDelete.text = ""
                        txtAreaVocabulario.text = ""
                        cbLinguagem.selectionModel.select(linguegem)
                    }
                }
                return null
            }
        }
        val processa = Thread(processarFila)
        progress.log.textProperty().bind(processarFila.messageProperty())
        progress.barraProgresso.progressProperty().bind(processarFila.progressProperty())
        processa.start()
    }

    @FXML
    private fun onBtnExportarFila() {
        if (btnExecutarFila.accessibleText.equals("PROCESSANDO", true) || btnExportarFila.accessibleText.equals("PROCESSANDO", true)) {
            desativar = true
            return
        }

        if (txtCaminhoExportar.text.isEmpty()) {
            AlertasPopup.alertaModal(stackPane, root, mutableListOf(), "Alerta", "Necessário informar um caminho para salvar o arquivo de exportação.")
            return
        }

        if (!validaProcessarFila())
            return

        desabilitaBotoes()
        btnProcessarTudo.isDisable = true
        btnExportarFila.accessibleText = "PROCESSANDO"
        btnExportarFila.text = "Pausar"
        desativar = false
        tbLista.items.clear()
        txtAreaSelect.isDisable = false
        txtAreaUpdate.isDisable = false
        txtAreaDelete.isDisable = false
        val progress = MenuPrincipalController.oController.criaBarraProgresso()
        progress!!.titulo.text = "Legendas - Processar Fila"
        val exportarFila: Task<Void> = object : Task<Void>() {
            var lista: MutableList<Processar> = mutableListOf()
            var fila: MutableList<FilaSQL> = mutableListOf()
            var i: Long = 0
            var x: Int = 0
            val pipe: String = txtPipe.text
            val arquivo: File? = if (txtCaminhoExportar.text.isEmpty()) null else File(txtCaminhoExportar.text)
            var linguegem: Language? = null

            @Override
            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                try {
                    fila = service.selectFila()
                    linguegem = cbLinguagem.selectionModel.selectedItem
                    if (linguegem != null && linguegem!! != Language.TODOS)
                        fila = fila.stream().filter { f -> f.linguagem == linguegem }.collect(Collectors.toList())

                    if (ccFiltrar.checkModel.checkedItems.isNotEmpty())
                        fila = fila.stream().filter { f -> ccFiltrar.checkModel.checkedItems.contains(f.nome) }.collect(Collectors.toList())

                    if (arquivo != null && !desativar) {
                        var select: List<FilaSQL> = fila.stream().filter(FilaSQL::isExporta)
                            .filter { f -> f.linguagem == Language.JAPANESE }
                            .collect(Collectors.toList())
                        if (select.isNotEmpty()) {
                            val file = File(arquivo.parent, arquivo.name.substring(0, arquivo.name.lastIndexOf(".")) + " Japones" + arquivo.name.substring(arquivo.name.lastIndexOf(".")))
                            updateMessage("Exportando arquivo " + file.name)
                            lista = mutableListOf()
                            for (item in select)
                                lista.addAll(service.comandoSelect(item.select))
                            exportar(pipe, lista, file)
                        }
                        select = fila.stream().filter(FilaSQL::isExporta).filter { f -> f.linguagem == Language.ENGLISH }.collect(Collectors.toList())
                        if (select.isNotEmpty()) {
                            val file = File(arquivo.parent, arquivo.name.substring(0, arquivo.name.lastIndexOf(".")) + " Ingles" + arquivo.name.substring(arquivo.name.lastIndexOf(".")))
                            updateMessage("Exportando arquivo " + file.name)
                            lista = mutableListOf()
                            for (item in select)
                                lista.addAll(service.comandoSelect(item.select))
                            exportar(pipe, lista, file)
                        }
                        updateMessage("Executando a limpeza.")
                        for (item in fila.stream().filter(FilaSQL::isLimpeza).collect(Collectors.toList()))
                            service.comandoDelete(item.delete)

                        Configuracao.caminhoSalvoArquivo = txtCaminhoExportar.text
                    }
                } catch (e: Exception) {
                    oLog.error("Erro ao processar a fila", e)
                } finally {
                    if (!desativar)
                        updateMessage("Concluído....")
                    Platform.runLater {
                        btnExportarFila.accessibleText = "PROCESSAR"
                        btnExportarFila.text = "Exportar fila"
                        habilitaBotoes()
                        btnProcessarTudo.isDisable = false
                        tbLista.isDisable = false
                        progress.barraProgresso.progressProperty().unbind()
                        progress.log.textProperty().unbind()
                        MenuPrincipalController.oController.destroiBarraProgresso(progress, "")
                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                        cbNome.editor.text = ""
                        txtAreaSelect.text = ""
                        txtAreaUpdate.text = ""
                        txtAreaDelete.text = ""
                        txtAreaVocabulario.text = ""
                        cbLinguagem.selectionModel.select(linguegem)
                    }
                }
                return null
            }
        }
        val exporta = Thread(exportarFila)
        progress.log.textProperty().bind(exportarFila.messageProperty())
        progress.barraProgresso.progressProperty().bind(exportarFila.progressProperty())
        exporta.start()
    }

    @Throws(IOException::class)
    private fun exportar(pipe: String, lista: List<Processar>, arquivo: File) {
        if (arquivo.exists()) arquivo.delete()
        arquivo.createNewFile()
        BufferedWriter(FileWriter(arquivo)).use { writer ->
            var index = 0
            for (item in lista) {
                index++
                writer.append(item.id).append(pipe).append(item.original)
                if (index < lista.size)
                    writer.newLine()
            }
            writer.flush()
        }
    }

    private fun carregaFiltros() {
        try {
            val nomes = service.nomes
            ccFiltrar.items.clear()
            ccFiltrar.items.addAll(nomes)
            cbNome.items.clear()
            cbNome.items.addAll(nomes)
        } catch (e: Exception) {
            oLog.error(e.message, e)
        }
    }

    private fun editaColunas() {
        tcId.setCellValueFactory(PropertyValueFactory("id"))
        tcOriginal.setCellValueFactory(PropertyValueFactory("original"))
        tcVocabulario.setCellValueFactory(PropertyValueFactory("vocabulario"))
        tcOriginal.setCellFactory(TextFieldTableCell.forTableColumn())
        tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn())
        tcVocabulario.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].vocabulario = e.newValue.trim()
            tbLista.refresh()
            tbLista.requestFocus()
        }
    }

    private fun linkaCelulas() = editaColunas()

    private lateinit var exportaListenner: ChangeListener<Boolean>
    private lateinit var limpezaListenner: ChangeListener<Boolean>
    private val robot: Robot = Robot()

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        cbLinguagem.items.addAll(Language.TODOS, Language.JAPANESE, Language.ENGLISH)
        cbLinguagem.selectionModel.selectFirst()
        cbLinguagem.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB)
        }

        linkaCelulas()
        carregaFiltros()

        val autoCompletePopup: JFXAutoCompletePopup<String> = JFXAutoCompletePopup()
        autoCompletePopup.suggestions.addAll(cbNome.items)
        autoCompletePopup.setSelectionHandler { event -> cbNome.setValue(event.getObject()) }
        cbNome.editor.textProperty().addListener { _, _, _ ->
            if (cbNome.isDisable)
                return@addListener

            autoCompletePopup.filter { item -> item.lowercase(Locale.getDefault()).contains(cbNome.editor.text.lowercase(Locale.getDefault())) }
            if (autoCompletePopup.filteredSuggestions.isEmpty() || cbNome.showingProperty().get() || cbNome.editor.text.isEmpty())
                autoCompletePopup.hide()
            else
                autoCompletePopup.show(cbNome.editor)
        }
        cbNome.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbNome.converter = object : StringConverter<String?>() {
            override fun toString(`object`: String?): String = `object`?.lowercase(Locale.getDefault()) ?: ""
            override fun fromString(string: String): String = string
        }

        btnProcessarTudo.accessibleText = "PROCESSAR"
        btnExecutarFila.accessibleText = "PROCESSAR"
        exportaListenner = ChangeListener<Boolean> { _, _, _ ->
            if (cbExporta.isSelected) {
                cbLimpeza.selectedProperty().removeListener(limpezaListenner)
                cbLimpeza.isSelected = false
                txtAreaSelect.isDisable = false
                txtAreaUpdate.isDisable = true
                txtAreaDelete.isDisable = true
                txtAreaUpdate.text = ""
                txtAreaDelete.text = ""
                cbLimpeza.selectedProperty().addListener(limpezaListenner)
            } else {
                if (!cbLimpeza.isSelected) {
                    txtAreaSelect.isDisable = false
                    txtAreaUpdate.isDisable = false
                    txtAreaDelete.isDisable = false
                }
            }
        }

        limpezaListenner = ChangeListener<Boolean> { _, _, _ ->
            if (cbLimpeza.isSelected) {
                cbExporta.selectedProperty().removeListener(exportaListenner)
                cbExporta.isSelected = false
                txtAreaSelect.isDisable = true
                txtAreaUpdate.isDisable = true
                txtAreaDelete.isDisable = false
                cbNome.editor.text = ""
                txtAreaSelect.text = ""
                txtAreaUpdate.text = ""
                cbExporta.selectedProperty().addListener(exportaListenner)
            } else {
                if (!cbExporta.isSelected) {
                    txtAreaSelect.isDisable = false
                    txtAreaUpdate.isDisable = false
                    txtAreaDelete.isDisable = false
                }
            }
        }
        cbExporta.selectedProperty().addListener(exportaListenner)
        cbLimpeza.selectedProperty().addListener(limpezaListenner)
        txtCaminhoExportar.text = Configuracao.caminhoSalvoArquivo
    }

    companion object {
        private val oLog: Logger = LoggerFactory.getLogger(LegendasVocabularioController::class.java)
        private var desativar = false
        val fxmlLocate: URL get() = LegendasVocabularioController::class.java.getResource("/view/legendas/LegendasVocabulario.fxml") as URL
    }
}