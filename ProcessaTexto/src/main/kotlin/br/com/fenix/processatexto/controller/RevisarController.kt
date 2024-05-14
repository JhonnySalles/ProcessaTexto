package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Database
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Notificacao
import br.com.fenix.processatexto.processar.*
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.RevisarInglesServices
import br.com.fenix.processatexto.service.RevisarJaponesServices
import br.com.fenix.processatexto.service.VocabularioInglesServices
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import br.com.fenix.processatexto.util.Utils
import com.google.firebase.database.*
import com.jfoenix.controls.*
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URL
import java.sql.SQLException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors


class RevisarController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var cbLinguagem: JFXComboBox<Language>

    @FXML
    private lateinit var lblRestantes: Label

    @FXML
    private lateinit var btnFormatar: JFXButton

    @FXML
    private lateinit var cbDuplicados: JFXCheckBox

    @FXML
    private lateinit var cbSubstituirKanji: JFXCheckBox

    @FXML
    private lateinit var btnExcluir: JFXButton

    @FXML
    private lateinit var btnSalvar: JFXButton

    @FXML
    private lateinit var btnNovo: JFXButton

    @FXML
    private lateinit var btnSalvarAux: JFXButton

    @FXML
    private lateinit var lvProcesssar: ListView<Triple<Vocabulario, Database, DatabaseReference>>

    @FXML
    private lateinit var cbLegenda: JFXCheckBox

    @FXML
    private lateinit var cbManga: JFXCheckBox

    @FXML
    private lateinit var cbNovel: JFXCheckBox

    @FXML
    private lateinit var txtVocabulario: JFXTextField

    @FXML
    private lateinit var txtSimilar: JFXTextField

    @FXML
    private lateinit var cbSimilar: JFXCheckBox

    @FXML
    private lateinit var txtPesquisar: JFXTextField

    @FXML
    private lateinit var cbCorrecao: JFXCheckBox

    @FXML
    private lateinit var btnTraduzir: JFXButton

    @FXML
    private lateinit var btnJapaneseTanoshi: JFXButton

    @FXML
    private lateinit var btnJapanDict: JFXButton

    @FXML
    private lateinit var btnJisho: JFXButton

    @FXML
    private lateinit var btnKanshudo: JFXButton

    @FXML
    private lateinit var txtAreaIngles: JFXTextArea

    @FXML
    private lateinit var txtAreaPortugues: JFXTextArea

    @FXML
    private lateinit var txtExclusao: JFXTextField

    private val revisarJapones = RevisarJaponesServices()
    private val vocabularioJapones = VocabularioJaponesServices()
    private val revisarIngles = RevisarInglesServices()
    private val vocabularioIngles = VocabularioInglesServices()

    private var similar: MutableList<Revisar> = mutableListOf()
    private val processsar: ObservableList<Triple<Vocabulario, Database, DatabaseReference>> = FXCollections.observableArrayList()
    private var revisando: Revisar? = null
    private var corrigindo: Vocabulario? = null

    private val robot: Robot = Robot()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://bilingual-reader-272ac-default-rtdb.firebaseio.com/")
    private var reference: DatabaseReference? = null

    @FXML
    private fun onBtnSalvar() {
        if (revisando == null && corrigindo == null || txtAreaPortugues.text.isEmpty())
            return

        var texto: String = txtAreaPortugues.text
        if (texto.contains("\n"))
            texto = texto.replace("\n", " ").trim()

        var error = false
        if (revisando != null) {
            revisando!!.portugues = texto
            revisando!!.ingles = Utils.removeDuplicate(revisando!!.ingles)

            val palavra: Vocabulario = Revisar.toVocabulario(revisando!!)

            for (obj in similar)
                obj.portugues = revisando!!.portugues

            val lista = Revisar.toVocabulario(similar)
            try {
                if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH)) {
                    vocabularioIngles.insert(palavra)
                    vocabularioIngles.insert(lista)
                    revisarIngles.delete(revisando!!)
                    revisarIngles.delete(similar)
                } else {
                    vocabularioJapones.insert(palavra)
                    vocabularioJapones.insert(lista)
                    revisarJapones.delete(revisando!!)
                    revisarJapones.delete(similar)
                }
                removeFiredac(palavra)
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                error = true
            }
        } else if (corrigindo != null) {
            corrigindo!!.portugues = texto
            try {
                if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
                    vocabularioIngles.insertOrUpdate(corrigindo!!)
                else
                    vocabularioJapones.insertOrUpdate(corrigindo!!)
                removeFiredac(corrigindo)
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                error = true
            }
        } else error = true
        if (!error) {
            limpaCampos()
            pesquisar()
        }
    }

    @FXML
    private fun onBtnExcluir() {
        if (revisando == null)
            return

        var error = false
        try {
            if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
                revisarIngles.delete(revisando!!)
            else
                revisarJapones.delete(revisando!!)
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            error = true
        }

        if (!error) {
            limpaCampos()
            pesquisar()
        }
    }

    @FXML
    private fun onBtnFormatar() {
        if (txtAreaPortugues.text.isEmpty())
            return

        if (cbDuplicados.isSelected)
            txtAreaPortugues.text = Utils.removeDuplicate(txtAreaPortugues.text)
        else
            txtAreaPortugues.text = Utils.normalize(txtAreaPortugues.text)
    }

    @FXML
    private fun onBtnNovo() {
        limpaCampos()
        pesquisar()
    }

    @FXML
    private fun onBtnTraduzir() {
        if (txtAreaIngles.text.isEmpty())
            return

        try {
            val texto =
                Utils.normalize(ScriptGoogle.translate(Language.ENGLISH.sigla, Language.PORTUGUESE.sigla, txtAreaIngles.text, MenuPrincipalController.controller.contaGoogle))
            txtAreaPortugues.text = Utils.normalize(texto)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
        }
    }

    @FXML
    private fun onBtnJapaneseTanoshi() {
        if (txtVocabulario.text.isEmpty())
            return

        txtAreaIngles.text = TanoshiJapanese.processa(txtVocabulario.text)
        onBtnTraduzir()
    }

    @FXML
    private fun onBtnTangorin() {
        if (txtVocabulario.text.isEmpty())
            return

        txtAreaIngles.text = Tangorin.processa(txtVocabulario.text)
        onBtnTraduzir()
    }

    @FXML
    private fun onBtnJapanDict() {
        if (txtVocabulario.text.isEmpty())
            return

        txtAreaIngles.text = JapanDict.processa(txtVocabulario.text)
        onBtnTraduzir()
    }

    @FXML
    private fun onBtnJisho() {
        if (txtVocabulario.text.isEmpty())
            return

        txtAreaIngles.text = Jisho.processa(txtVocabulario.text)
        onBtnTraduzir()
    }

    @FXML
    private fun onBtnKanshudo() {
        if (txtVocabulario.text.isEmpty())
            return

        txtAreaIngles.text = Kanshudo.processa(txtVocabulario.text)
        onBtnTraduzir()
    }

    val root: AnchorPane get() = apRoot

    fun setLegenda(ativo: Boolean) {
        cbLegenda.isSelected = ativo
    }

    fun setManga(ativo: Boolean) {
        cbManga.isSelected = ativo
    }

    fun setNovel(ativo: Boolean) {
        cbNovel.isSelected = ativo
    }

    private fun limpaCampos() {
        try {
            val qtd = if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
                revisarIngles.selectQuantidadeRestante()
            else
                revisarJapones.selectQuantidadeRestante()
            lblRestantes.text = "Restante $qtd palavras."
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            lblRestantes.text = "Restante 0 palavras."
        }
        corrigindo = null
        revisando = null
        limpaTextos()
        txtPesquisar.text = ""
    }

    private fun limpaTextos() {
        txtVocabulario.text = ""
        txtSimilar.text = ""
        txtAreaIngles.text = ""
        txtAreaPortugues.text = ""
    }

    @Throws(SQLException::class)
    private fun pesquisaCorrecao(pesquisar: String): Boolean {
        corrigindo = if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
            vocabularioIngles.select(pesquisar).orElse(null)
        else
            vocabularioJapones.select(pesquisar, pesquisar).orElse(null)

        return if (corrigindo != null && corrigindo!!.portugues.isNotEmpty()) {
            limpaTextos()
            cbCorrecao.isSelected = true
            if (corrigindo != null) {
                txtVocabulario.text = corrigindo!!.vocabulario
                txtAreaIngles.text = corrigindo!!.ingles
                txtAreaPortugues.text = Utils.normalize(corrigindo!!.portugues)
            }
            true
        } else false
    }

    @Throws(SQLException::class)
    private fun pesquisaRevisao(pesquisar: String): Boolean {
        revisando = if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
            revisarIngles.selectRevisar(pesquisar, cbLegenda.isSelected, cbManga.isSelected, cbNovel.isSelected).orElse(null)
        else
            revisarJapones.selectRevisar(pesquisar, cbLegenda.isSelected, cbManga.isSelected, cbNovel.isSelected).orElse(null)

        return if (revisando != null) {
            limpaTextos()
            cbCorrecao.isSelected = false
            if (cbSimilar.isSelected) {
                similar = if (cbLinguagem.selectionModel.selectedItem != null && cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
                    revisarIngles.selectSimilar(revisando!!.vocabulario, revisando!!.ingles)
                else
                    revisarJapones.selectSimilar(revisando!!.vocabulario, revisando!!.ingles)
            } else similar = ArrayList()
            txtVocabulario.text = revisando!!.vocabulario
            txtAreaIngles.text = revisando!!.ingles
            txtAreaPortugues.text = Utils.normalize(revisando!!.portugues)
            if (similar.size > 0)
                txtSimilar.text = similar.stream().map { it.vocabulario }.collect(Collectors.joining(", "))
            else
                txtSimilar.text = ""
            true
        } else false
    }

    fun pesquisar() {
        txtPesquisar.unFocusColor = Color.web("#106ebe")
        val pesquisar: String = txtPesquisar.text.trim()
        try {
            cbCorrecao.selectedProperty().removeListener(listenerCorrecao)
            if (pesquisar.isEmpty())
                pesquisaRevisao(pesquisar)
            else if (cbCorrecao.isSelected && pesquisaCorrecao(pesquisar))
                return
            else if (pesquisaRevisao(pesquisar))
                return
            else if (pesquisaCorrecao(pesquisar))
                return
            if (pesquisar.isNotEmpty()) {
                txtPesquisar.unFocusColor = Color.RED
                if (cbCorrecao.isSelected)
                    limpaCampos()
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        } finally {
            cbCorrecao.selectedProperty().addListener(listenerCorrecao)
        }
    }

    fun iniciaFirebase() {
        if (reference == null) {
            reference = database.getReference("anki")
            reference!!.addValueEventListener(object : ValueEventListener {
                @Override
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val lista: MutableList<Triple<Vocabulario, Database, DatabaseReference>> = mutableListOf()
                        for (linguagem in dataSnapshot.children) {
                            val database: Database = Database.valueOf(linguagem.key.uppercase(Locale.getDefault()))
                            for (vocab in linguagem.children) {
                                val obj: HashMap<String, String> = vocab.value as HashMap<String, String>
                                val vocabulario = Vocabulario(obj["vocabulario"]!!, obj["portugues"]!!)
                                lista.add(Triple(vocabulario, database, vocab.ref))
                            }
                        }
                        Platform.runLater { processsar.setAll(lista) }
                    } catch (e: Exception) {
                        LOGGER.error("Erro ao obter a lista de revisão do firebase.", e)
                    }
                }

                @Override
                override fun onCancelled(databaseError: DatabaseError?) {
                    LOGGER.error("Erro ao obter a database do firebase.", databaseError)
                }
            })
        }
    }

    private fun removeFiredac(vocabulario: Vocabulario?) {
        if (!processsar.isEmpty() && reference != null && vocabulario != null) {
            val item: Optional<Triple<Vocabulario, Database, DatabaseReference>> =
                processsar.parallelStream().filter { i -> i.first.vocabulario.equals(vocabulario.vocabulario, true) }.findFirst()
            if (item.isPresent) {
                processsar.remove(item.get())
                item.get().third.removeValueAsync()
            }
        }
    }

    private val allFlag = ".*"
    private val japaneseCopy = "([\u3000-\u303f\u3040-\u309f\u30a0-\u30ff\uff00-\uff9f\u4e00-\u9faf\u3400-\u4dbf]+)\\s-\\s(((?! - ).)*¹?)"
    private val englishCopy = "([A-Za-z0-9']+)\\s-\\s(((?! - ).)*¹?)"
    private val japanese = "[\u3041-\u9FAF]"
    private val notJapanese = "[A-Za-z0-9 ,;.à-úÀ-ú\\[\\]\\-\\(\\)]"

    private var frasePortugues = ""
    private var listenerCorrecao: ChangeListener<Boolean>? = null
    private var listenerReplace: ChangeListener<in String>? = null

    override fun initialize(arg0: URL, arg1: ResourceBundle) {
        cbLinguagem.items.addAll(Language.JAPANESE, Language.ENGLISH)
        cbLinguagem.selectionModel.selectFirst()
        cbLinguagem.setOnAction {
            when (cbLinguagem.selectionModel.selectedItem) {
                Language.JAPANESE -> cbSubstituirKanji.isSelected = true
                Language.ENGLISH -> cbSubstituirKanji.isSelected = false
                else -> {}
            }
            limpaCampos()
            pesquisar()
        }
        limpaCampos()
        txtAreaPortugues.textProperty().addListener { _, _, newVal ->
            if (cbSubstituirKanji.isSelected) {
                if (newVal.matches((allFlag + japanese + allFlag).toRegex()))
                    txtAreaPortugues.text = newVal.replaceFirst(" - ", "").replace(japanese.toRegex(), "")
            } else {
                if (newVal.contains("-"))
                    txtAreaPortugues.text = newVal.substring(newVal.indexOf("-")).replaceFirst("-", "").replace("¹", "").trim()
            }
        }
        listenerReplace = ChangeListener<String> { _, _, newVal ->
            try {
                txtPesquisar.textProperty().removeListener(listenerReplace)
                if (cbSubstituirKanji.isSelected) {
                    val matcher: Matcher = Pattern.compile(japaneseCopy).matcher(newVal.trim())
                    if (matcher.find()) {
                        txtPesquisar.text = matcher.group(1)
                        frasePortugues = matcher.group(2)
                    } else if (newVal.matches((allFlag + notJapanese + allFlag).toRegex())) {
                        val texto: String = newVal.replaceFirst(" - ", "").replace("¹", "")
                        txtPesquisar.text = texto.replace(notJapanese.toRegex(), "")
                        if (newVal.matches((allFlag + japanese + allFlag).toRegex()))
                            frasePortugues = texto.replace(japanese.toRegex(), "")
                    }
                } else {
                    val matcher: Matcher = Pattern.compile(englishCopy).matcher(newVal.trim())
                    if (matcher.find()) {
                        txtPesquisar.text = matcher.group(1)
                        frasePortugues = matcher.group(2)
                    } else if (newVal.contains("-")) {
                        val texto: String = newVal.substring(0, newVal.indexOf("-")).replaceFirst("-", "").trim()
                        txtPesquisar.text = texto
                        frasePortugues = newVal.replaceFirst(texto, "").replaceFirst("-", "").replace("¹", "").trim()
                    }
                }
            } finally {
                txtPesquisar.textProperty().addListener(listenerReplace)
            }
        }
        txtPesquisar.textProperty().addListener(listenerReplace)
        txtPesquisar.focusedProperty().addListener { _, oldVal, _ ->
            if (oldVal) {
                txtPesquisar.unFocusColor = Color.web("#106ebe")
                pesquisar()
                if (revisando != null && frasePortugues.isNotEmpty()) {
                    txtAreaPortugues.text = frasePortugues
                    onBtnFormatar()
                }
                frasePortugues = ""
            }
        }
        txtPesquisar.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtExclusao.textProperty().addListener { _, _, newVal ->
            val exclusao: String = newVal.trim().lowercase(Locale.getDefault())
            if (exclusao.contains("-"))
                txtExclusao.text = exclusao.substring(0, exclusao.indexOf("-")).replaceFirst("-", "").trim()
        }
        txtExclusao.focusedProperty().addListener { _, oldVal, _ ->
            if (oldVal) {
                if (txtExclusao.text != null && txtExclusao.text.isNotEmpty()) {
                    try {
                        val exclusao: String = txtExclusao.text.trim().lowercase(Locale.getDefault())
                        var sucesso = false
                        when (cbLinguagem.selectionModel.selectedItem) {
                            Language.JAPANESE -> if (exclusao.matches((allFlag + japanese + allFlag).toRegex())) {
                                vocabularioJapones.insertExclusao(txtExclusao.text.trim().lowercase(Locale.getDefault()))
                                sucesso = true
                            }
                            Language.ENGLISH -> if (exclusao.matches("$allFlag[A-Za-z\\d]$allFlag".toRegex())) {
                                vocabularioIngles.insertExclusao(txtExclusao.text.trim().lowercase(Locale.getDefault()))
                                sucesso = true
                            }
                            else -> {}
                        }
                        if (sucesso) {
                            txtExclusao.text = ""
                            Notificacoes.notificacao(Notificacao.SUCESSO, "Salvo.", "Exclusão salvo com sucesso. ($exclusao)")
                        } else
                            Notificacoes.notificacao(Notificacao.AVISO, "Alerta.", "Verifique a linguagem de exclusão ou a existência de caracteres especiais.")
                    } catch (e: SQLException) {
                        Notificacoes.notificacao(Notificacao.ERRO, "Erro.", "Erro ao salvar exclusão. ${e.message}")
                    }
                }
            }
        }
        txtExclusao.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        listenerCorrecao = ChangeListener<Boolean> { _, _, newVal -> if (newVal) pesquisar() else limpaCampos() }
        cbCorrecao.selectedProperty().addListener(listenerCorrecao)
        cbSimilar.selectedProperty().addListener { _, _, _ ->
            if (cbSimilar.isSelected)
                pesquisar()
            else {
                txtSimilar.text = ""
                similar = ArrayList()
            }
        }
        cbLegenda.selectedProperty().addListener { _, _, _ -> pesquisar() }
        cbManga.selectedProperty().addListener { _, _, _ -> pesquisar() }
        cbNovel.selectedProperty().addListener { _, _, _ -> pesquisar() }

        lvProcesssar.setCellFactory {
            object : ListCell<Triple<Vocabulario, Database, DatabaseReference>?>() {
                override fun updateItem(item: Triple<Vocabulario, Database, DatabaseReference>?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = null
                    if (empty || item == null)
                        graphic = null
                    else
                        text = item.first.vocabulario
                }
            }
        }

        lvProcesssar.onMouseClicked = EventHandler { click: MouseEvent ->
            if (click.clickCount == 1 && !lvProcesssar.items.isEmpty()) {
                val voc = lvProcesssar.selectionModel.selectedItem
                if (voc != null) {
                    when (voc.second) {
                        Database.INGLES -> {
                            if (!cbLinguagem.selectionModel.selectedItem.equals(Language.ENGLISH))
                                cbLinguagem.selectionModel.select(Language.ENGLISH)
                        }
                        Database.JAPONES -> {
                            if (!cbLinguagem.selectionModel.selectedItem.equals(Language.JAPANESE))
                                cbLinguagem.selectionModel.select(Language.JAPANESE)
                        }
                        else -> {}
                    }
                    txtPesquisar.text = voc.first.vocabulario
                    pesquisar()
                    txtAreaPortugues.text = voc.first.portugues
                    onBtnFormatar()
                    Toolkit.getDefaultToolkit()
                        .systemClipboard
                        .setContents(StringSelection(voc.first.vocabulario), null)
                }
            }
        }

        processsar.addListener(ListChangeListener { observable: ListChangeListener.Change<out Triple<Vocabulario, Database, DatabaseReference>?> -> lvProcesssar.setVisible(!observable.list.isEmpty()) } as ListChangeListener<in Triple<Vocabulario, Database, DatabaseReference>?>)

        lvProcesssar.items = processsar
        lvProcesssar.isVisible = false
        lvProcesssar.managedProperty().bind(lvProcesssar.visibleProperty())
        pesquisar()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(RevisarController::class.java)
        val fxmlLocate: URL
            get() = RevisarController::class.java.getResource("/view/Revisar.fxml")
    }
}