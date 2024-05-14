package br.com.fenix.processatexto.processar

import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.BaseController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.controller.legendas.LegendasImportarController
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Dicionario
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.model.enums.Site
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.RevisarInglesServices
import br.com.fenix.processatexto.service.RevisarJaponesServices
import br.com.fenix.processatexto.service.VocabularioInglesServices
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import br.com.fenix.processatexto.tokenizers.SudachiTokenizer
import br.com.fenix.processatexto.util.Utils
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import javafx.application.Platform
import javafx.concurrent.Task
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.regex.Pattern


class ProcessarLegendas(controller: BaseController) {

    private val LOGGER = LoggerFactory.getLogger(ProcessarLegendas::class.java)

    private val vocabularioJapones = VocabularioJaponesServices()
    private val revisarJapones = RevisarJaponesServices()
    private val vocabularioIngles = VocabularioInglesServices()
    private val revisarIngles = RevisarInglesServices()
    private val desmembra: ProcessarPalavra = ProcessarPalavra()

    private val controller: BaseController
    private var error: Boolean = false

    init {
        this.controller = controller
    }

    private lateinit var tokenizer: Tokenizer
    private lateinit var mode: SplitMode

    fun processarLegendas(frases: List<String>) {
        error = false
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Legendas - Processar"
        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val processarVocabulario = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    DictionaryFactory().create(
                        "", SudachiTokenizer.readAll(
                            FileInputStream(SudachiTokenizer.getPathSettings(MenuPrincipalController.controller.dicionario))
                        )
                    ).use { dict ->
                        tokenizer = dict.create()
                        mode = SudachiTokenizer.getModo(MenuPrincipalController.controller.modo)

                        var x = 0L
                        for (frase in frases) {
                            x++
                            updateProgress(x, frases.size.toLong())
                            updateMessage("Processando " + x + " de " + frases.size + " registros.")
                            processar(frase)
                        }
                    }
                } catch (e: IOException) {
                    LOGGER.error(e.message, e)
                    error = true
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.succeeded()
                if (error)
                    AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar a lista.")
                else
                    AlertasPopup.AvisoModal(controller.stackPane, controller.root, mutableListOf(), "Aviso", "Lista processada com sucesso.")
                progress.barraProgresso.progressProperty().unbind()
                progress.log.textProperty().unbind()
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de processamento do vocabulário: " + super.getMessage())
                print("Erro na thread de processamento do vocabulário: " + super.getMessage())
            }
        }
        progress.barraProgresso.progressProperty().bind(processarVocabulario.progressProperty())
        progress.log.textProperty().bind(processarVocabulario.messageProperty())
        val t = Thread(processarVocabulario)
        t.start()
    }

    fun processarJapones(dicionario: Dicionario, modo: Modo, frase: String): String {
        existe.clear()
        vocabulario.clear()
        var vocab = ""
        try {
            DictionaryFactory().create(
                "",
                SudachiTokenizer.readAll(FileInputStream(SudachiTokenizer.getPathSettings(dicionario)))
            ).use { dict ->
                tokenizer = dict.create()
                mode = SudachiTokenizer.getModo(modo)
                vocab = gerarVocabularioJapones(frase)
                if (vocab.isEmpty() && mode == SudachiTokenizer.getModo(Modo.C)) {
                    mode = SudachiTokenizer.getModo(Modo.B)
                    vocab = gerarVocabularioJapones(frase)
                }
                if (vocab.isEmpty() && mode == SudachiTokenizer.getModo(Modo.B)) {
                    mode = SudachiTokenizer.getModo(Modo.C)
                    vocab = gerarVocabularioJapones(frase)
                }
            }
        } catch (e: IOException) {
            vocab = ""
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar a lista.")
        } catch (e: SQLException) {
            vocab = ""
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar a lista.")
        }
        return vocab.trim()
    }

    private val pattern = ".*[\u4E00-\u9FAF].*".toRegex()
    private val japanese = ".*[\u3041-\u9FAF].*".toRegex()

    @Throws(SQLException::class)
    private fun processar(frase: String) {
        for (m in tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                val palavra: Optional<Vocabulario> = vocabularioJapones.select(m.surface(), m.dictionaryForm())
                if (palavra.isEmpty) {
                    val revisar = Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), "", revisado = false, isAnime = true, isManga = false, isNovel = false)
                    revisarJapones.insert(revisar)
                }
            }
        }
    }

    private val usarRevisar = true
    private val vocabHistorico: MutableSet<Vocabulario> = mutableSetOf()
    private val validaHistorico: MutableSet<String> = mutableSetOf()
    var vocabulario: MutableSet<String> = mutableSetOf()
    private val existe: MutableSet<String> = mutableSetOf()

    fun clearVocabulary() {
        vocabHistorico.clear()
        validaHistorico.clear()
        vocabulario.clear()
        existe.clear()
    }

    @Throws(SQLException::class)
    private fun gerarVocabularioJapones(frase: String): String {
        var vocabularios = ""
        for (m in tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                if (!existe.contains(m.dictionaryForm()) && !vocabularioJapones.existeExclusao(m.surface(), m.dictionaryForm())) {
                    existe.add(m.dictionaryForm())
                    var palavra: Vocabulario? = null
                    if (validaHistorico.contains(m.dictionaryForm()))
                        palavra = vocabHistorico.stream()
                            .filter { vocab -> m.dictionaryForm().equals(vocab.vocabulario, true) }
                            .findFirst().orElse(null)

                    if (palavra != null)
                        vocabularios += (m.dictionaryForm() + " - " + palavra.portugues) + " "
                    else {
                        palavra = vocabularioJapones.select(m.surface(), m.dictionaryForm()).orElse(null)
                        if (palavra != null) {
                            vocabularios += if (palavra.portugues.substring(0, 2).matches(japanese))
                                palavra.portugues + " "
                            else
                                (m.dictionaryForm() + " - " + palavra.portugues) + " "

                            // Usado apenas para correção em formas em branco.
                            if (palavra.formaBasica.isEmpty()) {
                                palavra.formaBasica = m.dictionaryForm()
                                palavra.leitura = m.readingForm()
                                vocabularioJapones.update(palavra)
                            }
                            validaHistorico.add(m.dictionaryForm())
                            vocabHistorico.add(palavra)
                            vocabulario.add(palavra.formaBasica)
                        } else if (usarRevisar) {
                            var revisar: Revisar? = revisarJapones.select(m.surface(), m.dictionaryForm()).orElse(null)
                            if (revisar != null) {
                                if (revisar.portugues.isNotEmpty() && revisar.portugues.substring(0, 2).matches(japanese))
                                    vocabularios += revisar.portugues + "¹ "
                                else {
                                    vocabularios += (m.dictionaryForm() + " - " + revisar.portugues) + "¹ "
                                    validaHistorico.add(m.dictionaryForm())
                                    vocabHistorico.add(Vocabulario(m.dictionaryForm(), revisar.portugues + "¹"))
                                }
                                vocabulario.add(m.dictionaryForm())
                            } else {
                                revisar = Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), "", revisado = false, isAnime = true, isManga = false, isNovel = false)
                                revisar.ingles = getSignificado(revisar.vocabulario)
                                if (revisar.ingles.isEmpty())
                                    revisar.ingles = getSignificado(revisar.formaBasica)
                                if (revisar.ingles.isEmpty())
                                    revisar.ingles = getDesmembrado(revisar.vocabulario)

                                if (revisar.ingles.isNotEmpty()) {
                                    try {
                                        Platform.runLater { MenuPrincipalController.controller.getLblLog().text = m.surface() + " : Obtendo tradução." }
                                        revisar.portugues = Utils.normalize(
                                            ScriptGoogle.translate(
                                                Language.ENGLISH.sigla,
                                                Language.PORTUGUESE.sigla,
                                                revisar.ingles,
                                                MenuPrincipalController.controller.contaGoogle
                                            )
                                        )
                                    } catch (e: IOException) {
                                        LOGGER.error(e.message, e)
                                    }
                                }
                                revisarJapones.insert(revisar)
                                if (revisar.portugues.isNotEmpty() && revisar.portugues.substring(0, 2).matches(japanese))
                                    vocabularios += revisar.portugues + "¹ "
                                else {
                                    vocabularios += (m.dictionaryForm() + " - " + revisar.portugues) + "¹ "
                                    validaHistorico.add(m.dictionaryForm())
                                    vocabHistorico.add(Vocabulario(m.dictionaryForm(), revisar.portugues + "¹"))
                                }
                                vocabulario.add(m.dictionaryForm())
                            }
                        }
                    }
                }
            }
        }
        return vocabularios
    }

    private fun getSignificado(kanji: String): String {
        if (kanji.trim().isEmpty()) return ""
        Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$kanji : Obtendo significado." }
        var resultado = ""
        when (MenuPrincipalController.controller.site) {
            Site.TODOS -> {
                resultado = TanoshiJapanese.processa(kanji)
                if (resultado.isEmpty())
                    resultado = JapanDict.processa(kanji)
                if (resultado.isEmpty())
                    resultado = Jisho.processa(kanji)
            }
            Site.JAPANESE_TANOSHI -> resultado = TanoshiJapanese.processa(kanji)
            Site.JAPANDICT -> resultado = JapanDict.processa(kanji)
            Site.JISHO -> resultado = Jisho.processa(kanji)
            else -> {}
        }
        return resultado
    }

    private fun getDesmembrado(palavra: String): String {
        var resultado = ""
        Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$palavra : Desmembrando a palavra." }
        resultado = processaPalavras(desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.B), Modo.B)
        if (resultado.isEmpty()) resultado = processaPalavras(desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)
        return resultado
    }

    private fun processaPalavras(palavras: List<String>, modo: Modo): String {
        var desmembrado = ""
        try {
            for (palavra in palavras) {
                var resultado = getSignificado(palavra)
                if (resultado.trim().isNotEmpty())
                    desmembrado += "$palavra - $resultado; "
                else if (modo == Modo.B) {
                    resultado = processaPalavras(desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)
                    if (resultado.trim().isNotEmpty())
                        desmembrado += resultado
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            desmembrado = ""
        }
        return desmembrado
    }

    // -------------------------------------------------------------------------------------------------
    fun processarIngles(frase: String): String {
        var frase = frase
        existe.clear()
        vocabulario.clear()
        var vocab = ""
        try {
            val ignore: Pattern = Pattern.compile("[\\d]|[^a-zA-Z0-9_'çãáàéèíìúù]")
            if (frase.isNotEmpty()) {
                frase = frase.lowercase(Locale.getDefault())
                val palavras = frase.split(" ")
                    .filter { txt -> !txt.trim().contains(" ") && txt.isNotEmpty() }
                    .distinct().toSet()

                for (palavra in palavras) {
                    if (ignore.matcher(palavra).find())
                        continue
                    vocab += gerarVocabularioIngles(palavra)
                }
            }
        } catch (e: SQLException) {
            vocab = ""
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar a lista.")
        }
        return vocab.trim()
    }

    @Throws(SQLException::class)
    private fun gerarVocabularioIngles(texto: String): String {
        var vocab = ""
        if (!existe.contains(texto) && !vocabularioIngles.existeExclusao(texto)) {
            existe.add(texto)
            var palavra: Vocabulario? = null
            if (validaHistorico.contains(texto))
                palavra = vocabHistorico.stream()
                    .filter { vc -> texto.equals(vc.vocabulario, true) }
                    .findFirst().orElse(null)
            if (palavra != null)
                vocab = "• " + texto.substring(0, 1).uppercase(Locale.getDefault()) + texto.substring(1) + " - " + palavra.portugues + " "
            else {
                palavra = vocabularioIngles.select(texto).orElse(null)
                if (palavra != null) {
                    vocab = "• " + texto.substring(0, 1).uppercase(Locale.getDefault()) + texto.substring(1) + " - " + palavra.portugues + " "
                    validaHistorico.add(texto)
                    vocabHistorico.add(palavra)
                    vocabulario.add(texto)
                } else if (usarRevisar) {
                    var revisar: Revisar? = revisarIngles.select(texto).orElse(null)
                    if (revisar != null) {
                        vocab = "• " + texto.substring(0, 1).uppercase(Locale.getDefault()) + texto.substring(1) + " - " + revisar.portugues + "¹ "
                        validaHistorico.add(texto)
                        vocabHistorico.add(Vocabulario(texto, revisar.portugues + "¹"))
                        vocabulario.add(texto)
                    } else {
                        revisar = Revisar(texto, "", "", "", revisado = false, isAnime = true, isManga = false, isNovel = false)
                        try {
                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$texto : Obtendo tradução." }
                            revisar.portugues = Utils.normalize(
                                ScriptGoogle.translate(
                                    Language.ENGLISH.sigla,
                                    Language.PORTUGUESE.sigla,
                                    texto,
                                    MenuPrincipalController.controller.contaGoogle
                                )
                            )
                        } catch (e: IOException) {
                            LOGGER.error(e.message, e)
                        }
                        revisarIngles.insert(revisar)
                        vocab = "• " + texto.substring(0, 1).uppercase(Locale.getDefault()) + texto.substring(1) + " - " + revisar.portugues + "¹ "
                        validaHistorico.add(texto)
                        vocabHistorico.add(Vocabulario(texto, revisar.portugues + "¹"))
                        vocabulario.add(texto)
                    }
                }
            }
        }
        return vocab
    }
}