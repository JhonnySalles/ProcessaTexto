package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.components.animation.Animacao
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.model.entities.processatexto.japones.Estatistica
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.service.EstatisticaServices
import com.google.common.collect.Sets
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.css.PseudoClass
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTreeTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.robot.Robot
import javafx.util.Callback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors


open class EstatisticaController : Initializable {

    @FXML
    private lateinit var apGlobal: AnchorPane

    @FXML
    private lateinit var rootStackPane: StackPane

    @FXML
    protected lateinit var root: AnchorPane

    @FXML
    private lateinit var btnBanco: JFXButton

    @FXML
    private lateinit var imgConexaoBase: ImageView

    @FXML
    private lateinit var txtVocabulario: JFXTextField

    @FXML
    private lateinit var btnProcessar: JFXButton

    @FXML
    private lateinit var btnGerarTabelas: JFXButton

    @FXML
    private lateinit var txtPesquisa: JFXTextField

    @FXML
    private lateinit var ckbMarcarTodos: JFXCheckBox

    @FXML
    private lateinit var treePalavras: TreeTableView<Estatistica>

    @FXML
    private lateinit var treecTipo: TreeTableColumn<Estatistica, String>

    @FXML
    private lateinit var treecLeitura: TreeTableColumn<Estatistica, String>

    @FXML
    private lateinit var treecQuantidade: TreeTableColumn<Estatistica, String>

    @FXML
    private lateinit var treecPercentual: TreeTableColumn<Estatistica, String>

    @FXML
    private lateinit var treecMedia: TreeTableColumn<Estatistica, String>

    @FXML
    private lateinit var treecGerar: TreeTableColumn<Estatistica, Boolean>

    @FXML
    private lateinit var tbVocabulario: TableView<Tabela>

    @FXML
    private lateinit var tcVocabulario: TableColumn<Tabela, String>

    @FXML
    private lateinit var tcLeitura: TableColumn<Tabela, String>

    @FXML
    private lateinit var tcTabela: TableColumn<Tabela, String>

    private val listaPalavra: MutableList<List<Estatistica>> = mutableListOf()
    private val combinacoes: MutableList<Tabela> = mutableListOf()
    private var obsLCombinacoes: ObservableList<Tabela>? = null
    private var estatisticaServ: EstatisticaServices? = null
    private val animacao = Animacao()

    private val robot: Robot = Robot()
    val pesquisa: PseudoClass = PseudoClass.getPseudoClass("pesquisa")

    @FXML
    private fun onBtnVerificaConexao() {
        verificaConexao()
    }

    @FXML
    private fun onBtnProcessar() {
        if (txtVocabulario.text.isNotEmpty()) {
            processarVocabulario()
        }
    }

    @FXML
    private fun onBtnGerarTabela() {
        geraProcessaLista()
    }

    @FXML
    private fun onBtnMarcarTodos() {
        marcarTodosFilhos(treePalavras.root, ckbMarcarTodos.isSelected)
        treePalavras.refresh()
    }

    private fun marcarTodosFilhos(treeItem: TreeItem<Estatistica>, newValue: Boolean) {
        treeItem.value.isGerar = newValue
        treeItem.children.forEach { treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue) }
    }

    private fun pesquisaVocabulario() {
        if (txtPesquisa.text.isNotEmpty()) {
            try {
                combinacoes.addAll(estatisticaServ!!.pesquisa(txtPesquisa.text))
                obsLCombinacoes = FXCollections.observableArrayList(combinacoes)
                tbVocabulario.setItems(obsLCombinacoes)
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                AlertasPopup.ErroModal(rootStackPane, root, mutableListOf(), "Não foi possível realizar a pesquisa.", e.message!!)
            }
        }
    }

    // UNICODE RANGE : DESCRIPTION
    //
    // 3000-303F : punctuation
    // 3040-309F : hiragana
    // 30A0-30FF : katakana
    // FF00-FFEF : Full-width roman + half-width katakana
    // 4E00-9FAF : Common and uncommon kanji
    //
    // Non-Japanese punctuation/formatting characters commonly used in Japanese text
    // 2605-2606 : Stars
    // 2190-2195 : Arrows
    // u203B : Weird asterisk thing
    private val pattern = ".*[\u4E00-\u9FAF].*".toRegex()
    private fun processarVocabulario() {
        listaPalavra.clear()
        var hiragana = ""
        var letra: String
        try {
            val root: TreeItem<Estatistica> = TreeItem(Estatistica("Kanjis"))
            for (i in 0 until txtVocabulario.text.length) {
                letra = txtVocabulario.text.substring(i, i + 1)
                if (!letra.matches(pattern)) {
                    hiragana += letra
                } else {
                    // Antes de iniciar o processo, verificar se tem hiraganas adicionados
                    if (hiragana.isNotEmpty()) {
                        adicionaHiragana(root, hiragana)
                        hiragana = ""
                    }
                    val titulo = Estatistica(letra)
                    val kanji: TreeItem<Estatistica> = TreeItem(titulo)
                    val estatistica: MutableList<Estatistica> = estatisticaServ!!.select(letra)
                    if (estatistica.size <= 0) {
                        val novo = Estatistica()
                        novo.kanji = letra
                        novo.leitura = letra
                        novo.isGerar = true
                        estatistica.add(novo)
                        kanji.children.add(TreeItem(novo))
                    } else {
                        for (ls in estatistica) kanji.children.add(TreeItem(ls))
                    }
                    root.children.add(kanji)
                    listaPalavra.add(estatistica)
                }
            }
            // Caso não possua mais kanjis será necessário adicionar os hiragana
            if (hiragana.isNotEmpty()) adicionaHiragana(root, hiragana)
            treePalavras.setRoot(root)
            treePalavras.setShowRoot(false)
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal(rootStackPane, root, mutableListOf(), "Erro ao processar vocabulario", e.message!!)
        }
    }

    private fun adicionaHiragana(root: TreeItem<Estatistica>, hiragana: String) {
        val novo = Estatistica()
        novo.kanji = hiragana
        novo.leitura = hiragana
        novo.isGerar = true

        val titulo = Estatistica(hiragana)
        val treeHiragana: TreeItem<Estatistica> = TreeItem(titulo)
        treeHiragana.children.add(TreeItem(novo))
        val estatistica: MutableList<Estatistica> = mutableListOf()
        estatistica.add(novo)
        root.children.add(treeHiragana)
        listaPalavra.add(estatistica)
    }

    fun geraProcessaLista() {
        combinacoes.clear()
        val selecao: MutableList<Set<Estatistica>> = mutableListOf()
        for (ls in listaPalavra) {
            val listaPalavra: MutableSet<Estatistica> = ls.stream().filter { c -> c.isGerar }.collect(Collectors.toSet())
            selecao.add(listaPalavra)
        }

        val result: Set<MutableList<Estatistica>> = Sets.cartesianProduct(selecao)
        if (result.isEmpty())
            AlertasPopup.AlertaModal(rootStackPane, root, mutableListOf(), "Lista vazia.", "Necessário selecionar ao menos uma leitura de cada kanji.")
        else {
            for (ls in result)
                combinacoes.add(Tabela(ls))
            obsLCombinacoes = FXCollections.observableArrayList(combinacoes)
            tbVocabulario.setItems(obsLCombinacoes)
        }
    }

    fun verificaConexao() {
        animacao.tmLineImageBanco.play()

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val verificaConexao: Task<String> = object : Task<String>() {
            @Override
            @Throws(Exception::class)
            override fun call(): String {
                return JdbcFactory.testaConexao(Conexao.PROCESSA_TEXTO)
            }

            @Override
            override fun succeeded() {
                animacao.tmLineImageBanco.stop()
                val conectado: String = value
                if (conectado.isNotEmpty())
                    imgConexaoBase.image = imgAnimaBancoConectado
                else
                    imgConexaoBase.image = imgAnimaBancoErro
            }
        }
        val t = Thread(verificaConexao)
        t.isDaemon = true
        t.start()
    }

    private fun configuraListenert() {
        txtVocabulario.onKeyPressed = EventHandler { ke ->
            if (ke.code.equals(KeyCode.ENTER)) {
                onBtnProcessar()
                robot.keyPress(KeyCode.TAB)
            }
        }
        txtPesquisa.onKeyPressed = EventHandler { ke ->
            if (ke.code.equals(KeyCode.ENTER)) {
                pesquisaVocabulario()
            }
        }
    }

    private fun editaColunas() {
        tcLeitura.cellFactory = TextFieldTableCell.forTableColumn()
        tcLeitura.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].leitura = e.newValue
            tbVocabulario.requestFocus()
        }
        tcTabela.cellFactory = TextFieldTableCell.forTableColumn()
        tcTabela.setOnEditCommit { e ->
            e.tableView.items[e.tablePosition.row].tabela = e.newValue
            tbVocabulario.requestFocus()
        }
        tbVocabulario.setRowFactory {
            val row: TableRow<Tabela> = object : TableRow<Tabela>() {
                @Override
                override fun updateItem(item: Tabela?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null) {
                        style = ""
                        pseudoClassStateChanged(pesquisa, false)
                    } else {
                        pseudoClassStateChanged(pesquisa, item.isPesquisa)
                    }
                }
            }
            row
        }

        // ==== Gerar (CHECK-BOX) ===
        treecGerar.cellValueFactory = Callback<TreeTableColumn.CellDataFeatures<Estatistica, Boolean>, ObservableValue<Boolean>> { param ->
                val treeItem: TreeItem<Estatistica> = param.value
                val esta: Estatistica = treeItem.value
                val booleanProp = SimpleBooleanProperty(esta.isGerar)
                booleanProp.addListener { _, _, newValue -> esta.isGerar = newValue }
            booleanProp
            }
        treecGerar.cellFactory = Callback<TreeTableColumn<Estatistica, Boolean>, TreeTableCell<Estatistica, Boolean>> {
            val cell: CheckBoxTreeTableCell<Estatistica, Boolean> = CheckBoxTreeTableCell<Estatistica, Boolean>()
            cell.alignment = Pos.CENTER
            cell.styleClass.add("hide-non-leaf") // Insere o tipo para ser invisivel
            cell
        }

        // A função irá deixar invisíveis todos os checkbox que não sejam folha
        treePalavras.setRowFactory {
            object : TreeTableRow<Estatistica>() {
                init {
                    val listener: ChangeListener<Boolean> = ChangeListener<Boolean> { _, _, newValue -> pseudoClassStateChanged(FOLHA, newValue) }
                    treeItemProperty().addListener { _, oldItem, newItem ->
                        oldItem?.leafProperty()?.removeListener(listener)
                        if (newItem != null) {
                            newItem.leafProperty().addListener(listener)
                            listener.changed(null, null, newItem.isLeaf)
                        } else {
                            listener.changed(null, null, false)
                        }
                    }
                }
            }
        }
    }

    private fun linkaCelulas() {
        tcVocabulario.setCellValueFactory(PropertyValueFactory("vocabulario"))
        tcLeitura.setCellValueFactory(PropertyValueFactory("leitura"))
        tcTabela.setCellValueFactory(PropertyValueFactory("tabela"))
        treecTipo.setCellValueFactory(TreeItemPropertyValueFactory("tipo"))
        treecLeitura.setCellValueFactory(TreeItemPropertyValueFactory("leitura"))
        treecQuantidade.setCellValueFactory(TreeItemPropertyValueFactory("quantidade"))
        treecPercentual.setCellValueFactory(TreeItemPropertyValueFactory("percentual"))
        treecMedia.setCellValueFactory(TreeItemPropertyValueFactory("media"))
        treecGerar.setCellValueFactory(TreeItemPropertyValueFactory("gerar"))
        editaColunas()
    }

    @Override
    override fun initialize(location: URL, resources: ResourceBundle) {
        animacao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoEspera)
        linkaCelulas()
        configuraListenert()
        verificaConexao()
        estatisticaServ = EstatisticaServices()
    }

    // Classe interna para utilização somente aqui.
    class Tabela {
        var vocabulario: String? = null
        var leitura: String? = null
        var tabela: String? = null
        private val estatistica: MutableList<Estatistica> = mutableListOf()
        var isPesquisa = false

        fun getEstatistica(): MutableList<Estatistica> = estatistica

        fun setEstatistica(estatistica: MutableList<Estatistica>) {
            this.estatistica.clear()
            this.estatistica.addAll(estatistica)
            vocabulario = ""
            leitura = ""
            var reading = ""
            var chars = ""
            var type = ""
            var perc = ""
            var classe = ""
            for (es in estatistica) {
                vocabulario += es.kanji
                leitura += es.leitura
                classe = if (es.tipo.equals("ON", true))
                    " class=\"o" + es.corSequencial + "\">"
                else if (es.tipo.equals("KUN", true))
                    " class=\"k" + es.corSequencial + "\">"
                else ">"

                if (es.leitura.equals(es.kanji, true)) {
                    reading += "<td></td>"
                    chars += "<td>" + es.kanji + "</td>"
                    type += "<td></td>"
                    perc += "<td></td>"
                } else {
                    reading += "<td" + classe + es.leitura + "</td>"
                    chars += "<td>" + es.kanji + "</td>"
                    type += "<td>" + (if (es.tipo.equals("Irreg.", true)) "Irr" else es.tipo) + "</td>"
                    perc += "<td>" + es.percentual + "%</td>"
                }
            }
            tabela = ("<table class=\"detailed-word\"><tbody>" + "<tr class=\"reading\">" + reading + "</tr>"
                    + "<tr class=\"chars\">" + chars + "</tr>" + "<tr class=\"type\">" + type + "</tr>"
                    + "<tr class=\"perc\">" + perc + "</tr>" + "</tbody></table>")
        }

        fun addEstatistica(estatistica: Estatistica) = this.estatistica.add(estatistica)

        constructor() {
            vocabulario = ""
            leitura = ""
            tabela = ""
        }

        constructor(estatistica: MutableList<Estatistica>) {
            setEstatistica(estatistica)
        }

        constructor(vocabulario: String?, leitura: String?, tabela: String?, pesquisa: Boolean) {
            this.vocabulario = vocabulario
            this.leitura = leitura
            this.tabela = tabela
            isPesquisa = pesquisa
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(EstatisticaController::class.java)
        val imgAnimaBanco: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataBase_48.png"))

        val imgAnimaBancoEspera: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataEspera_48.png"))
        val imgAnimaBancoErro: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataSemConexao_48.png"))
        val imgAnimaBancoConectado: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataConectado_48.png"))

        private val FOLHA: PseudoClass = PseudoClass.getPseudoClass("leaf")
        val fxmlLocate: URL get() = EstatisticaController::class.java.getResource("/view/Estatistica.fxml") as URL
        val iconLocate: String get() = "/images/icoTextoJapones_128.png"
    }
}