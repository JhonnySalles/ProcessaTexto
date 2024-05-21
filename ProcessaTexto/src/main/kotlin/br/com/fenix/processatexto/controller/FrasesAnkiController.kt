package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Notificacao
import br.com.fenix.processatexto.model.enums.Tipo
import br.com.fenix.processatexto.service.RevisarJaponesServices
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import br.com.fenix.processatexto.tokenizers.SudachiTokenizer
import br.com.fenix.processatexto.util.Utils
import com.jfoenix.controls.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import br.com.fenix.processatexto.processar.kanjiStatics.ImportaEstatistica
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors


open class FrasesAnkiController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var stackPane: StackPane

    @FXML
    private lateinit var apConteinerRoot: AnchorPane

    @FXML
    private lateinit var btnSalvar: JFXButton

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var btnFormatarTabela: JFXButton

    @FXML
    private lateinit var btnEstatistica: JFXButton

    @FXML
    private lateinit var btnCorrecao: JFXButton

    @FXML
    private lateinit var btnImportar: JFXButton

    @FXML
    private lateinit var ckListaExcel: JFXCheckBox

    @FXML
    private lateinit var cbTipo: JFXComboBox<Tipo>

    @FXML
    private lateinit var txtVocabulario: JFXTextField

    @FXML
    private lateinit var txtAreaOrigem: JFXTextArea

    @FXML
    private lateinit var txtAreaDestino: JFXTextArea

    @FXML
    private lateinit var txtExclusoes: JFXTextField

    @FXML
    private lateinit var lblExclusoes: Label

    @FXML
    private lateinit var lblRegistros: Label

    @FXML
    private lateinit var tbVocabulario: TableView<Vocabulario>

    @FXML
    private lateinit var tcVocabulario: TableColumn<Vocabulario, String>

    @FXML
    private lateinit var tcPortugues: TableColumn<Vocabulario, String>

    @FXML
    private lateinit var tcIngles: TableColumn<Vocabulario, String>

    private val vocabServ = VocabularioJaponesServices()
    private val revisaServ = RevisarJaponesServices()

    private var vocabulario: Vocabulario? = null

    var excluido: Set<String> = mutableSetOf()
        private set

    private val robot: Robot = Robot()

    @FXML
    private fun onBtnSalvar() = salvarTexto()

    @FXML
    private fun onBtnImportar() = ImportaEstatistica.importa()

    @FXML
    private fun onBtnEstatistica() {
        try {
            val loader = FXMLLoader()
            loader.location = EstatisticaController.fxmlLocate
            val newAnchorPane: AnchorPane = loader.load()
            val mainScene = Scene(newAnchorPane) // Carrega a scena
            mainScene.fill = Color.BLACK
            val stage = Stage()
            stage.scene = mainScene // Seta a cena principal
            stage.title = "Gerar estatisticas"
            stage.initStyle(StageStyle.DECORATED)
            stage.initModality(Modality.WINDOW_MODAL)
            stage.icons.add(Image(javaClass.getResourceAsStream(EstatisticaController.iconLocate)))
            stage.show() // Mostra a tela.
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            println("Erro ao abrir a tela de estatistica.")
        }
    }

    @FXML
    private fun onBtnCorrecao() = CorrecaoController.abreTelaCorrecao(stackPane, apConteinerRoot)

    @FXML
    private fun onBtnProcessar() {
        if (btnProcessar.accessibleText.equals("PROCESSAR", true))
            processaTexto()
        else
            SudachiTokenizer.DESATIVAR = true
    }

    @FXML
    private fun onBtnFormatarLista() {
        try {
            tbVocabulario.isDisable = true
            for (vocabulario in tbVocabulario.items) {
                if (vocabulario.portugues.trim().isNotEmpty())
                    vocabulario.portugues = Utils.removeDuplicate(vocabulario.portugues)
                if (vocabulario.ingles.trim().isNotEmpty())
                    vocabulario.ingles = Utils.removeDuplicate(vocabulario.ingles)
            }
        } finally {
            tbVocabulario.refresh()
            tbVocabulario.isDisable = false
        }
    }

    fun setPalavra(palavra: String) {
        try {
            vocabulario = vocabServ.select(palavra).orElse(Vocabulario(palavra))
            if (vocabulario!!.portugues.isEmpty()) {
                if (txtAreaOrigem.text.isNotEmpty())
                    txtVocabulario.unFocusColor = Color.RED
                else
                    txtVocabulario.unFocusColor = Color.web("#106ebe")
                txtVocabulario.text = ""
                txtVocabulario.isEditable = true
            } else {
                txtVocabulario.text = vocabulario!!.portugues
                txtVocabulario.isEditable = false
                txtVocabulario.unFocusColor = Color.web("#106ebe")
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            Notificacoes.notificacao(Notificacao.ERRO, "Erro pesquisar a palavra.", palavra)
            txtVocabulario.unFocusColor = Color.RED
        }
    }

    fun desabilitaBotoes() {
        cbTipo.isDisable = true
        btnCorrecao.isDisable = true
        btnEstatistica.isDisable = true
        btnImportar.isDisable = true
        btnSalvar.isDisable = true
        tbVocabulario.isDisable = true
        btnFormatarTabela.isDisable = true
        btnProcessar.accessibleText = "PAUSAR"
        btnProcessar.text = "Pausar"
    }

    fun habilitaBotoes() {
        cbTipo.isDisable = false
        btnCorrecao.isDisable = false
        btnEstatistica.isDisable = false
        btnImportar.isDisable = false
        btnSalvar.isDisable = false
        tbVocabulario.isDisable = false
        btnFormatarTabela.isDisable = false
        btnProcessar.accessibleText = "PROCESSAR"
        btnProcessar.text = "Processar lista"
    }

    fun limpaVocabulario() {
        vocabulario = null
        txtVocabulario.text = ""
        txtVocabulario.isEditable = false
        txtVocabulario.unFocusColor = Color.web("#106ebe")
    }

    val tipo: Tipo
        get() = cbTipo.selectionModel.selectedItem
    val textoOrigem: String
        get() = txtAreaOrigem.text

    fun setTextoDestino(texto: String) {
        txtAreaDestino.text = texto
    }

    fun setAviso(aviso: String) = Notificacoes.notificacao(Notificacao.AVISO, "Aviso.", aviso)

    val isListaExcel: Boolean
        get() = ckListaExcel.isSelected

    fun setVocabulario(lista: List<Vocabulario?>) {
        tbVocabulario.items.clear()
        tbVocabulario.items.addAll(lista)
        if (tbVocabulario.items.isEmpty()) tbVocabulario.items.add(Vocabulario())
        lblRegistros.text = "Vocab.: " + lista.size
        tbVocabulario.refresh()
    }

    private fun salvaVocabulario() {
        if (txtVocabulario.isEditable)
            if (txtVocabulario.text.trim().isNotEmpty()) {
                vocabulario!!.portugues = txtVocabulario.text.trim()
                try {
                    vocabServ.insertOrUpdate(vocabulario!!)
                    Notificacoes.notificacao(Notificacao.SUCESSO, "Salvamento vocabulário concluído.", txtVocabulario.text)
                    txtVocabulario.unFocusColor = Color.LIME
                    txtVocabulario.isEditable = false
                } catch (e: SQLException) {
                    LOGGER.error(e.message, e)
                    Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar vocabulario.", txtVocabulario.text)
                    txtVocabulario.unFocusColor = Color.RED
                    txtVocabulario.isEditable = true
                }
            } else if (txtAreaOrigem.text.isNotEmpty()) {
                txtVocabulario.unFocusColor = Color.RED
                txtVocabulario.isEditable = true
            }
    }

    private fun salvaExclusao() {
        if (txtExclusoes.text.trim().isNotEmpty()) {
            try {
                if (!vocabServ.existeExclusao(txtExclusoes.text.trim())) {
                    excluido = vocabServ.insertExclusao(txtExclusoes.text).selectExclusao()
                    lblExclusoes.text = excluido.toString()
                    Notificacoes.notificacao(Notificacao.SUCESSO, "Salvamento exclusão concluído.", txtExclusoes.text)
                } else Notificacoes.notificacao(
                    Notificacao.ALERTA, "Palavra já existe na exclusão.",
                    txtExclusoes.text
                )
                txtExclusoes.unFocusColor = Color.LIME
                txtExclusoes.text = ""
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar vocabulário de exclusão.", txtExclusoes.text)
                txtExclusoes.unFocusColor = Color.RED
            }
        }
    }

    @Throws(SQLException::class)
    private fun atualizaExclusao() {
        excluido = vocabServ.selectExclusao()
        lblExclusoes.text = excluido.toString()
    }

    private fun processaTexto() {
        try {
            if (excluido == null)
                atualizaExclusao()
            val tokenizer = SudachiTokenizer()
            tokenizer.processa(this)
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            Notificacoes.notificacao(Notificacao.ERRO, "Erro.", "Erro ao pesquisar vocabulário excluído.")
        }
    }

    private fun salvarTexto() {
        if (tbVocabulario.items.size > 0) {
            try {
                val salvar = tbVocabulario.items.stream().filter { e -> e.portugues.isNotEmpty() }.collect(Collectors.toList())
                vocabServ.insert(salvar)
                var itensSalvo = ""
                for (item in salvar) {
                    txtAreaDestino.text = txtAreaDestino.text.replace(item.formaBasica + " \\*\\*", item.formaBasica + " - " + item.portugues)
                    itensSalvo += item.toString()
                    revisaServ.delete(item.vocabulario)
                }
                if (salvar.size !== tbVocabulario.items.size)
                    tbVocabulario.items.removeIf(salvar::contains)
                else {
                    tbVocabulario.items.clear()
                    tbVocabulario.items.add(Vocabulario())
                }
                if (itensSalvo.isEmpty()) Notificacoes.notificacao(
                    Notificacao.AVISO,
                    "Nenhum item encontrado.",
                    "Nenhum item com tradução encontrada."
                ) else Notificacoes.notificacao(Notificacao.SUCESSO, "Salvamento texto concluído.", itensSalvo.substring(0, itensSalvo.lastIndexOf(", ")) + ".")
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                Notificacoes.notificacao(Notificacao.ERRO, "Erro.", "Erro ao salvar os novos vocabulários.")
            }
        } else Notificacoes.notificacao(Notificacao.AVISO, "Aviso.", "Lista vazia.")
    }

    private fun adicionaUltimaLinha() {
        tbVocabulario.addEventFilter(KeyEvent.KEY_RELEASED) { event ->
            if (event.code === KeyCode.DOWN) {
                @SuppressWarnings("unchecked")
                val pos: TablePosition<Vocabulario, *> = tbVocabulario.focusModel.focusedCell as TablePosition<Vocabulario, *>
                if (pos.row === -1) {
                    tbVocabulario.selectionModel.select(0)
                } else if (pos.row === tbVocabulario.items.size - 1) {
                    addRow()
                }
            }
        }
    }

    fun addRow() {
        @SuppressWarnings("unchecked")
        val pos: TablePosition<Vocabulario, *> = tbVocabulario.focusModel.focusedCell as TablePosition<Vocabulario, *>
        tbVocabulario.selectionModel.clearSelection()
        val data = Vocabulario()
        tbVocabulario.items.add(data)
        tbVocabulario.selectionModel.select(tbVocabulario.items.size - 1, pos.tableColumn)
        tbVocabulario.scrollTo(data)
    }

    private fun editaColunas() {
        tcVocabulario.setCellValueFactory(PropertyValueFactory("vocabulario"))
        tcPortugues.setCellValueFactory(PropertyValueFactory("portugues"))
        tcIngles.setCellValueFactory(PropertyValueFactory("ingles"))
        tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn())
        tcVocabulario.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].vocabulario = e.newValue.trim()
            tbVocabulario.requestFocus()
        }
        tcPortugues.setCellFactory(TextFieldTableCell.forTableColumn())
        tcPortugues.setOnEditCommit { e ->
            var frase = ""
            if (e.newValue.trim().isNotEmpty())
                frase = Utils.normalize(e.newValue.trim())
            e.tableView.items[e.tablePosition.row].portugues = frase
            tbVocabulario.refresh()
            tbVocabulario.requestFocus()
        }
        tbVocabulario.setRowFactory {
            val row: TableRow<Vocabulario> = TableRow()
            row.setOnMouseClicked { event -> if (event.clickCount === 2 && row.isEmpty) addRow() }
            row
        }
        tcIngles.setCellFactory(TextFieldTableCell.forTableColumn())
    }

    private fun linkaCelulas() {
        editaColunas()
        adicionaUltimaLinha()
        val vocabulario = mutableListOf<Vocabulario>()
        vocabulario.add(Vocabulario())
        val observable: ObservableList<Vocabulario> = FXCollections.observableArrayList(vocabulario)
        tbVocabulario.setItems(observable)
    }

    private fun configuraListenert() {
        txtAreaOrigem.focusedProperty().addListener { _, oldVal, _ -> if (oldVal && !cbTipo.selectionModel.selectedItem.equals(Tipo.VOCABULARIO)) processaTexto() }
        txtVocabulario.focusedProperty().addListener { _, oldVal, _ ->
            if (oldVal) {
                txtVocabulario.unFocusColor = Color.web("#106ebe")
                salvaVocabulario()
            }
        }
        txtExclusoes.focusedProperty().addListener { _, oldVal, _ -> if (oldVal) txtExclusoes.unFocusColor = Color.web("#106ebe") }
        txtVocabulario.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ENTER)) {
                salvaVocabulario()
                robot.keyPress(KeyCode.TAB)
            }
        }
        txtExclusoes.setOnKeyPressed { ke ->
            if (ke.code.equals(KeyCode.ENTER)) {
                salvaExclusao()
                robot.keyPress(KeyCode.TAB)
            }
        }
    }

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        linkaCelulas()
        configuraListenert()
        cbTipo.items.addAll(Tipo.values())
        cbTipo.selectionModel.select(Tipo.TEXTO)

        /* Setando as variáveis para o alerta padrão. */
        AlertasPopup.rootStackPane = stackPane
        AlertasPopup.nodeBlur = apConteinerRoot
        Notificacoes.rootAnchorPane = apRoot
        btnProcessar.accessibleText = "PROCESSAR"
        btnProcessar.text = "Processar lista"
        try {
            atualizaExclusao()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(FrasesAnkiController::class.java)
        val fxmlLocate: URL get() = FrasesAnkiController::class.java.getResource("/view/FrasesAnki.fxml") as URL
        val iconLocate: String get() = "/images/icoTextoJapones_128.png"
    }
}