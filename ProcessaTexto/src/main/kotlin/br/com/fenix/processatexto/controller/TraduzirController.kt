package br.com.fenix.processatexto.controller


import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.model.enums.Site
import br.com.fenix.processatexto.processar.*
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.RevisarJaponesServices
import br.com.fenix.processatexto.util.Utils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.AnchorPane
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors


class TraduzirController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var txtQuantidadeRegistros: JFXTextField

    @FXML
    private lateinit var btnProcessarTudo: JFXButton

    @FXML
    private lateinit var btnTraduzir: JFXButton

    @FXML
    private lateinit var btnSalvar: JFXButton

    @FXML
    private lateinit var btnAtualizar: JFXButton

    @FXML
    private lateinit var btnJapaneseTanoshi: JFXButton

    @FXML
    private lateinit var btnJapanDict: JFXButton

    @FXML
    private lateinit var btnJisho: JFXButton

    @FXML
    private lateinit var btnKanshudo: JFXButton

    @FXML
    private lateinit var btnTangorin: JFXButton

    @FXML
    private lateinit var ckbDesmembrar: JFXCheckBox

    @FXML
    private lateinit var ckbMarcarTodos: JFXCheckBox

    @FXML
    private lateinit var tbVocabulario: TableView<Revisar>

    @FXML
    private lateinit var tcVocabulario: TableColumn<Revisar, String>

    @FXML
    private lateinit var tcIngles: TableColumn<Revisar, String>

    @FXML
    private lateinit var tcPortugues: TableColumn<Revisar, String>

    @FXML
    private lateinit var tcRevisado: TableColumn<Revisar, CheckBox>

    private val service = RevisarJaponesServices()
    private val processar = ProcessarPalavra()

    @FXML
    private fun onBtnSalvar() {
        try {
            MenuPrincipalController.controller.getLblLog().text = "Salvando...."

            btnSalvar.isDisable = true
            btnAtualizar.isDisable = true
            tbVocabulario.isDisable = true
            btnProcessarTudo.isDisable = true
            btnTraduzir.isDisable = true
            btnJapaneseTanoshi.isDisable = true
            btnJapanDict.isDisable = true
            btnJisho.isDisable = true
            btnTangorin.isDisable = true
            btnKanshudo.isDisable = true

            val update: List<Revisar> = tbVocabulario.items.stream()
                .filter { revisar -> revisar.isRevisado }
                .collect(Collectors.toList())

            // List<Vocabulario> salvar = update.stream().map(revisar ->
            // Revisar.toVocabulario(revisar))
            // .collect(Collectors.toList());
            service.insertOrUpdate(update)

            // VocabularioServices service = new VocabularioServices();
            // service.insertOrUpdate(salvar);
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal("Erro", "Erro ao salvar as atualizações.")
        } finally {
            MenuPrincipalController.controller.getLblLog().text = "Salvamento concluido."

            btnSalvar.isDisable = false
            btnAtualizar.isDisable = false
            tbVocabulario.isDisable = false
            btnProcessarTudo.isDisable = false
            btnTraduzir.isDisable = false
            btnJapaneseTanoshi.isDisable = false
            btnJapanDict.isDisable = false
            btnJisho.isDisable = false
            btnTangorin.isDisable = false
            btnKanshudo.isDisable = false

            AlertasPopup.AvisoModal("Salvo", "Salvo com sucesso.")
            onBtnAtualizar()
        }
    }

    @FXML
    private fun onBtnAtualizar() {
        try {
            MenuPrincipalController.controller.getLblLog().text = "Atualizando....."
            tbVocabulario.items = FXCollections.observableArrayList(service.selectTraduzir(Integer.valueOf(txtQuantidadeRegistros.text)))
            MenuPrincipalController.controller.getLblLog().text = ""
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal("Erro", "Erro ao pesquisar as revisões.")
        }
    }

    @FXML
    private fun onBtnMarcarTodos() {
        tbVocabulario.items.forEach { e -> e.isRevisado = ckbMarcarTodos.isSelected }
        tbVocabulario.refresh()
    }

    private fun getSignificado(kanji: String): String {
        if (kanji.trim().isEmpty())
            return ""

        var resultado = ""
        when (MenuPrincipalController.controller.site) {
            Site.TODOS -> {
                resultado = TanoshiJapanese.processa(kanji)
                if (resultado.isEmpty())
                    resultado = JapanDict.processa(kanji)
                if (resultado.isEmpty())
                    resultado = Jisho.processa(kanji)
                if (resultado.isEmpty())
                    resultado = Kanshudo.processa(kanji)
            }
            Site.JAPANESE_TANOSHI -> resultado = TanoshiJapanese.processa(kanji)
            Site.JAPANDICT -> resultado = JapanDict.processa(kanji)
            Site.JISHO -> resultado = Jisho.processa(kanji)
            Site.TANGORIN -> resultado = Tangorin.processa(kanji)
            Site.KANSHUDO -> resultado = Kanshudo.processa(kanji)
            else -> {}
        }
        return resultado
    }

    private fun processaPalavras(palavras: List<String>, modo: Modo): String {
        var desmembrado = ""
        for (palavra in palavras) {
            var resultado = getSignificado(palavra)
            if (resultado.trim().isNotEmpty())
                desmembrado += "$palavra - $resultado; "
            else if (modo == Modo.B) {
                resultado = processaPalavras(processar.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)
                if (resultado.trim().isNotEmpty())
                    desmembrado += resultado
            }
        }
        return desmembrado
    }

    private fun getDesmembrado(palavra: String): String {
        var resultado = ""

        resultado = processaPalavras(processar.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.B), Modo.B)
        if (resultado.isEmpty())
            resultado = processaPalavras(processar.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)

        return resultado
    }

    @FXML
    private fun onBtnProcessarTudo() {
        if (btnProcessarTudo.accessibleText.equals("PROCESSANDO", false)) {
            desativar = true
            return
        }
        if (tbVocabulario.items.isEmpty())
            return

        btnSalvar.isDisable = true
        btnAtualizar.isDisable = true
        tbVocabulario.isDisable = true
        btnTraduzir.isDisable = true
        btnJapaneseTanoshi.isDisable = true
        btnJapanDict.isDisable = true
        btnJisho.isDisable = true
        btnTangorin.isDisable = true
        btnKanshudo.isDisable = true
        ckbDesmembrar.isDisable = true
        btnProcessarTudo.accessibleText = "PROCESSANDO"
        btnProcessarTudo.text = "Pausar"
        desativar = false
        desmembrar = ckbDesmembrar.isSelected

        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress?.run { this.titulo.text = "Tradução" }

        val processarTudo: Task<Void> = object : Task<Void>() {

            var lista: MutableList<Revisar> = mutableListOf()
            var i: Long = 0

            @Override
            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                lista = ArrayList(tbVocabulario.items)
                try {
                    for (item in lista) {
                        i++
                        updateMessage("Processando item " + i + " de " + lista.size)
                        updateProgress(i, lista.size.toLong())

                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size.toLong(), Type.NORMAL)
                        }

                        if (!item.isRevisado) {
                            if (item.ingles.isEmpty()) {
                                item.ingles = getSignificado(item.vocabulario)

                                if (item.ingles.isEmpty())
                                    item.ingles = getSignificado(item.formaBasica)

                                if (desmembrar && item.ingles.isEmpty())
                                    item.ingles = getSignificado(getDesmembrado(item.vocabulario))
                            }

                            if (item.portugues.isEmpty() && item.ingles.isNotEmpty()) {
                                try {
                                    item.portugues = Utils.normalize(
                                        ScriptGoogle.translate(
                                            Language.ENGLISH.sigla,
                                            Language.PORTUGUESE.sigla,
                                            item.ingles,
                                            MenuPrincipalController.controller.contaGoogle
                                        )
                                    )
                                } catch (e: IOException) {
                                    LOGGER.error(e.message, e)
                                }
                            }

                            item.isRevisado = true
                        }
                        if (desativar)
                            break
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                } finally {
                    Platform.runLater {
                        tbVocabulario.items = FXCollections.observableArrayList(lista)

                        btnProcessarTudo.accessibleText = "PROCESSAR"
                        btnProcessarTudo.text = "Processar tudo"
                        btnSalvar.isDisable = false
                        btnAtualizar.isDisable = false
                        tbVocabulario.isDisable = false
                        btnTraduzir.isDisable = false
                        btnJapaneseTanoshi.isDisable = false
                        btnJapanDict.isDisable = false
                        btnJisho.isDisable = false
                        btnTangorin.isDisable = false
                        btnKanshudo.isDisable = false
                        ckbDesmembrar.isDisable = false

                        progress?.run { this.log.textProperty().unbind() }
                        progress?.run { this.barraProgresso.progressProperty().unbind() }

                        MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage())

                        tbVocabulario.refresh()
                    }
                }
                return null
            }
        }

        val processa = Thread(processarTudo)
        progress?.run { this.log.textProperty().bind(processarTudo.messageProperty()) }
        progress?.run { this.barraProgresso.progressProperty().bind(processarTudo.progressProperty()) }
        processa.start()
    }

    @FXML
    private fun onBtnTraduzir() {
        if (tbVocabulario.items.isEmpty())
            return

        if (tbVocabulario.selectionModel.selectedItem == null || tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            return

        try {
            tbVocabulario.selectionModel.selectedItem.portugues = Utils.normalize(
                ScriptGoogle.translate(
                    Language.ENGLISH.sigla, Language.PORTUGUESE.sigla,
                    tbVocabulario.selectionModel.selectedItem.ingles,
                    MenuPrincipalController.controller.contaGoogle
                )
            )

            tbVocabulario.refresh()
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
        }
    }

    @FXML
    private fun onBtnJapaneseTanoshi() {
        if (tbVocabulario.items.isEmpty() || tbVocabulario.selectionModel.selectedItem == null)
            return

        tbVocabulario.selectionModel.selectedItem.ingles = TanoshiJapanese.processa(tbVocabulario.selectionModel.selectedItem.vocabulario)

        if (tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            tbVocabulario.selectionModel.selectedItem.ingles = TanoshiJapanese.processa(tbVocabulario.selectionModel.selectedItem.formaBasica)

        if (ckbDesmembrar.isSelected && tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            tbVocabulario.selectionModel.selectedItem.ingles = getDesmembrado(tbVocabulario.selectionModel.selectedItem.vocabulario)

        tbVocabulario.refresh()
    }

    @FXML
    private fun onBtnTangorin() {
        if (tbVocabulario.items.isEmpty() || tbVocabulario.selectionModel.selectedItem == null)
            return

        tbVocabulario.selectionModel.selectedItem.ingles = Tangorin.processa(tbVocabulario.selectionModel.selectedItem.vocabulario)
        tbVocabulario.refresh()
    }

    @FXML
    private fun onBtnJapanDict() {
        if (tbVocabulario.items.isEmpty() || tbVocabulario.selectionModel.selectedItem == null)
            return

        tbVocabulario.selectionModel.selectedItem.ingles = JapanDict.processa(tbVocabulario.selectionModel.selectedItem.vocabulario)
        if (tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            tbVocabulario.selectionModel.selectedItem.ingles = JapanDict.processa(tbVocabulario.selectionModel.selectedItem.formaBasica)

        if (ckbDesmembrar.isSelected && tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            tbVocabulario.selectionModel.selectedItem.ingles = getDesmembrado(tbVocabulario.selectionModel.selectedItem.vocabulario)

        tbVocabulario.refresh()
    }

    @FXML
    private fun onBtnJisho() {
        if (tbVocabulario.items.isEmpty() || tbVocabulario.selectionModel.selectedItem == null)
            return

        tbVocabulario.selectionModel.selectedItem
            .ingles = Jisho.processa(tbVocabulario.selectionModel.selectedItem.vocabulario)

        if (tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            tbVocabulario.selectionModel.selectedItem.ingles = Jisho.processa(tbVocabulario.selectionModel.selectedItem.formaBasica)

        if (ckbDesmembrar.isSelected && tbVocabulario.selectionModel.selectedItem.ingles.isEmpty())
            tbVocabulario.selectionModel.selectedItem.ingles = getDesmembrado(tbVocabulario.selectionModel.selectedItem.vocabulario)

        tbVocabulario.refresh()
    }

    @FXML
    private fun onBtnKanshudo() {
        if (tbVocabulario.items.isEmpty() || tbVocabulario.selectionModel.selectedItem == null)
            return

        tbVocabulario.selectionModel.selectedItem.ingles = Kanshudo.processa(tbVocabulario.selectionModel.selectedItem.vocabulario)
        tbVocabulario.refresh()
    }

    val root: AnchorPane get() = apRoot

    private fun editaColunas() {

        tcVocabulario.cellValueFactory = PropertyValueFactory("vocabulario")
        tcIngles.cellValueFactory = PropertyValueFactory("ingles")
        tcPortugues.cellValueFactory = PropertyValueFactory("portugues")
        tcVocabulario.cellFactory = TextFieldTableCell.forTableColumn()
        tcIngles.cellFactory = TextFieldTableCell.forTableColumn()

        tcIngles.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].ingles = e.newValue.trim()
            if (e.newValue.trim().isNotEmpty()) {
                e.tableView.items[e.tablePosition.row].isRevisado = true
                if (e.tableView.items[e.tablePosition.row].portugues.isEmpty()) {
                    try {
                        e.tableView.items[e.tablePosition.row].portugues = Utils.normalize(
                            ScriptGoogle.translate(
                                Language.ENGLISH.sigla,
                                Language.PORTUGUESE.sigla, e.newValue.trim(),
                                MenuPrincipalController.controller.contaGoogle
                            )
                        )

                        tbVocabulario.refresh()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
            tbVocabulario.requestFocus()
        }
        tcPortugues.cellFactory = TextFieldTableCell.forTableColumn()
        tcPortugues.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].portugues = e.newValue.trim()

            if (e.newValue.trim().isNotEmpty())
                e.tableView.items[e.tablePosition.row].isRevisado = true

            tbVocabulario.requestFocus()
        }
        tcRevisado.cellValueFactory = PropertyValueFactory("revisado")
    }

    private fun linkaCelulas() {
        editaColunas()
    }

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        txtQuantidadeRegistros.textProperty().addListener { _, oldValue, newValue ->
            if (newValue != null && !newValue.matches("\\d*".toRegex()))
                txtQuantidadeRegistros.text = oldValue
            else if (newValue != null && newValue.isEmpty())
                txtQuantidadeRegistros.text = "0"
        }
        linkaCelulas()
        btnProcessarTudo.accessibleText = "PROCESSAR"
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(TraduzirController::class.java)

        private var desativar = false
        private var desmembrar = false

        val fxmlLocate: URL get() = TraduzirController::class.java.getResource("/view/Traduzir.fxml")
    }
}