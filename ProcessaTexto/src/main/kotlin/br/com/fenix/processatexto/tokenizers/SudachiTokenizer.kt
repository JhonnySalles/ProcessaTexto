package br.com.fenix.processatexto.tokenizers

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.FrasesAnkiController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.*
import br.com.fenix.processatexto.processar.JapanDict
import br.com.fenix.processatexto.processar.Jisho
import br.com.fenix.processatexto.processar.TanoshiJapanese
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.KanjiServices
import br.com.fenix.processatexto.service.RevisarJaponesServices
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer
import javafx.application.Platform
import javafx.concurrent.Task
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.sql.SQLException
import java.util.*
import java.util.concurrent.TimeUnit

class SudachiTokenizer {

    private val LOGGER = LoggerFactory.getLogger(SudachiTokenizer::class.java)

    private var google: Api = Api.API_GOOGLE
    private var vocabServ: VocabularioJaponesServices? = null
    private var kanjiServ: KanjiServices? = null
    private val repetido: MutableSet<String> = mutableSetOf()
    private val vocabNovo: MutableList<Vocabulario> = mutableListOf()
    
    private lateinit var controller: FrasesAnkiController

    private val atualizaBarraWindows: Runnable = Runnable { Platform.runLater { TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, max, Type.NORMAL) } }

    companion object {
        private var i = 0L
        private var max = 0L
        var DESATIVAR = false
        fun getPathSettings(dicionario: Dicionario): String {
            var settings_path: String = Paths.get("").toAbsolutePath().toString()
            settings_path += when (dicionario) {
                Dicionario.SAMLL -> "/sudachi_smalldict.json"
                Dicionario.CORE -> "/sudachi_coredict.json"
                Dicionario.FULL -> "/sudachi_fulldict.json"
                else -> "/sudachi_fulldict.json"
            }
            return settings_path
        }

        fun getModo(modo: Modo): Tokenizer.SplitMode {
            return when (modo) {
                Modo.A -> Tokenizer.SplitMode.A
                Modo.B -> Tokenizer.SplitMode.B
                Modo.C -> Tokenizer.SplitMode.C
                else -> Tokenizer.SplitMode.C
            }
        }

        @Throws(IOException::class)
        fun readAll(input: InputStream): String {
            val isReader = InputStreamReader(input, StandardCharsets.UTF_8)
            val reader = BufferedReader(isReader)
            val sb = StringBuilder()
            while (true) {
                val line: String = reader.readLine() ?: break
                sb.append(line)
            }
            return sb.toString()
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
    var tokenizer: Tokenizer? = null

    @Throws(SQLException::class)
    private fun processaTexto() {
        val texto = controller.textoOrigem.split("\n")
        var processado = ""
        vocabNovo.clear()
        repetido.clear()
        google = MenuPrincipalController.controller.contaGoogle
        controller.setPalavra(texto[0])
        
        try {
            FileInputStream(getPathSettings(MenuPrincipalController.controller.dicionario)).use { input ->
                DictionaryFactory().create("", readAll(input)).use { dict ->
                    tokenizer = dict.create()
                    i = 0
                    max = texto.size.toLong()
                    val mode = getModo(MenuPrincipalController.controller.modo)
                    for (txt in texto) {
                        if (txt !== texto[0] && txt.isNotEmpty()) {
                            processado += processaTokenizer(mode, txt, false)
                            processado += "\n\n\n"
                        }
                        i++
                        atualizaProgresso()
                    }
                    concluiProgresso(false)
                }
            }
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            concluiProgresso(true)
            AlertasPopup.ErroModal("Erro ao processar textos", e.message!!)
        }
        controller.setTextoDestino(processado)
        controller.setVocabulario(vocabNovo)
        processaListaNovo(false)
    }

    @Throws(SQLException::class)
    private fun processaKanji() {
        val texto = controller.textoOrigem.split("\n")
        var processado = ""
        vocabNovo.clear()
        repetido.clear()
        google = MenuPrincipalController.controller.contaGoogle
        controller.setPalavra(texto[0])
        try {
            FileInputStream(getPathSettings(MenuPrincipalController.controller.dicionario)).use { input ->
                DictionaryFactory().create("", readAll(input)).use { dict ->
                    tokenizer = dict.create()
                    i = 0
                    max = texto.size.toLong()
                    val mode = getModo(MenuPrincipalController.controller.modo)
                    for (txt in texto) {
                        if (txt.isNotEmpty()) {
                            val tokenizer = processaTokenizer(mode, txt, false)
                            var kanjis = ""
                            for (letra in if (tokenizer.isEmpty()) txt.toCharArray() else tokenizer.toCharArray()) {
                                val kanji: String = letra.toString()
                                if (kanji.matches(pattern)) {
                                    val palavra = kanjiServ!!.select(kanji)
                                    if (palavra.isPresent)
                                        kanjis += kanji + " " + palavra.get().palavra + " "
                                }
                            }
                            processado += if (tokenizer.isEmpty())
                                kanjis.trim() + "\n\n"
                            else
                                "$tokenizer${if (kanjis.isNotEmpty()) " (" + kanjis.trim() + ")" else ""}"
                        }
                        i++
                        atualizaProgresso()
                    }
                    concluiProgresso(false)
                }
            }
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            concluiProgresso(true)
            AlertasPopup.ErroModal("Erro ao processar textos", e.message!!)
        }
        controller.setTextoDestino(processado)
        controller.setVocabulario(vocabNovo)
        processaListaNovo(false)
    }

    @Throws(SQLException::class)
    private fun processaMusica() {
        MenuPrincipalController.controller.setAviso("Sudachi - Processar música")
        val processar: Task<Void> = object : Task<Void>() {

            lateinit var texto: List<String>
            var processado = ""
            var dictionario = Dicionario.FULL
            var mode = Tokenizer.SplitMode.C
            var erro = false

            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                DESATIVAR = false
                vocabNovo.clear()

                Platform.runLater {
                    texto = controller.textoOrigem.split("\n")
                    dictionario = MenuPrincipalController.controller.dicionario
                    mode = getModo(MenuPrincipalController.controller.modo)
                    google = MenuPrincipalController.controller.contaGoogle
                    controller.limpaVocabulario()
                    controller.desabilitaBotoes()
                }
                try {
                    DictionaryFactory().create("", readAll(FileInputStream(getPathSettings(dictionario)))).use { dict ->
                        tokenizer = dict.create()
                        i = 1
                        max = texto.size.toLong()
                        for (txt in texto) {
                            updateProgress(i, max)
                            atualizaBarraWindows.run()
                            i++
                            if (txt.isNotEmpty()) {
                                processado += txt
                                processado += processaTokenizer(mode, txt, true)
                                processado += "\n\n\n"
                            } else processado += "\n"
                            if (DESATIVAR)
                                return null
                        }
                    }
                } catch (e: IOException) {
                    erro = true
                    LOGGER.error(e.message, e)
                    Platform.runLater { AlertasPopup.ErroModal("Erro ao processar o textos", e.message!!) }
                } catch (e: SQLException) {
                    erro = true
                    LOGGER.error(e.message, e)
                    Platform.runLater { AlertasPopup.ErroModal("Erro de conexao", e.message!!) }
                } finally {
                    Platform.runLater {
                        if (erro) TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), 1, 1, Type.ERROR) else {
                            i = 1
                            max = 1
                            updateProgress(i, max)
                            atualizaBarraWindows.run()
                        }
                        controller.setVocabulario(vocabNovo)
                        controller.setTextoDestino(processado)
                        controller.habilitaBotoes()
                    }
                    if (erro)
                        TimeUnit.SECONDS.sleep(5)
                    Platform.runLater {
                        concluiProgresso(false)
                        processaListaNovo(true)
                    }
                }
                return null
            }
        }
        val processa = Thread(processar)
        processa.start()
    }

    @Throws(SQLException::class)
    private fun processaVocabulario() {
        MenuPrincipalController.controller.setAviso("SUDACHI - Processar vocabulário obtendo frase do site Tanoshi Japanese.")
        val processar: Task<Void> = object : Task<Void>() {
            var palavra = ""
            var palavras = listOf("")
            var vocabulario = ""
            var dictionario: Dicionario = Dicionario.FULL
            var mode = Tokenizer.SplitMode.C
            var erro = false
            var isExcel = false

            @Override
            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                DESATIVAR = false
                var ingles: String
                var portugues: String
                var leitura: String
                var expressao: String
                var significado: String
                var links: String
                var frase: Array<Array<String>>
                vocabNovo.clear()

                Platform.runLater {
                    palavras = controller.textoOrigem.split("\n")
                    dictionario = MenuPrincipalController.controller.dicionario
                    mode = getModo(MenuPrincipalController.controller.modo)
                    google = MenuPrincipalController.controller.contaGoogle
                    isExcel = controller.isListaExcel
                    controller.limpaVocabulario()
                    controller.desabilitaBotoes()
                }

                try {
                    DictionaryFactory().create("", readAll(FileInputStream(getPathSettings(dictionario)))).use { dict ->
                        tokenizer = dict.create()
                        i = 1
                        max = palavras.size.toLong().plus(1)
                        for (texto in palavras) {
                            ingles = ""
                            portugues = ""
                            leitura = ""
                            if (texto.contains("|")) {
                                val textos = texto.replace("||", "|").split("\\|")
                                palavra = textos[0]
                                ingles = textos[1] + "."
                                if (textos.size > 2 && textos[2].isNotEmpty())
                                    leitura = textos[2]
                            } else palavra = texto
                            Platform.runLater {
                                MenuPrincipalController.controller.getLblLog().text = "Processando vocabulário $palavra - $i de $max"
                            }
                            updateProgress(i, max)
                            atualizaBarraWindows.run()
                            i++
                            repetido.clear()
                            if (palavra.trim().isNotEmpty()) {
                                links = ""
                                if (ingles.isNotEmpty()) {
                                    if (ingles.contains(";"))
                                        ingles = ingles.replace(";", ", ")
                                    if (ingles.contains("..") && !ingles.contains("..."))
                                        ingles = ingles.replace("..", ".")

                                    ingles = ingles.substring(0, 1).uppercase(Locale.getDefault()) + ingles.substring(1)
                                    portugues = ScriptGoogle.translate(Language.ENGLISH.sigla, Language.PORTUGUESE.sigla, ingles, google)

                                    if (portugues.isNotEmpty())
                                        portugues = portugues.substring(0, 1).uppercase(Locale.getDefault()) + portugues.substring(1)
                                }
                                expressao = palavra + if (isExcel) "<br><br>" else "\n\n"
                                significado = portugues + if (isExcel) "<br><br>" else "\n\n"
                                frase = TanoshiJapanese.getFrase(palavra.trim())
                                for (i in 0..1) {
                                    if (frase[i][0].isNotEmpty()) {
                                        var processado = processaTokenizer(mode, frase[i][0], false)
                                        var traduzido: String = ScriptGoogle.translate(Language.ENGLISH.sigla, Language.PORTUGUESE.sigla, frase[i][1], google)

                                        if (frase[i][0].contains("&quot;")) frase[i][0] =
                                            frase[i][0].replace("&quot;", "'") else if (frase[i][0].contains(";")) frase[i][0] += frase[i][0].replace(";", ",")

                                        if (processado.contains(";"))
                                            processado = processado.replace(";", ",")

                                        if (traduzido.contains("&quot;"))
                                            traduzido = traduzido.replace("&quot;", "'")

                                        if (traduzido.contains(";"))
                                            traduzido = traduzido.replace(";", ",")

                                        if (isExcel) {
                                            expressao += frase[i][0] + "<br><br>"
                                            significado += (if (processado.isBlank()) "" else "$processado<br><br>") + traduzido + "<br><br>"
                                            links += if (frase[i][2].isNotEmpty()) frase[i][2] + ";" else ""
                                        } else {
                                            expressao += frase[i][0]
                                            significado += "$processado $traduzido"
                                            links += frase[i][2].ifEmpty { "" }
                                        }
                                    } else {
                                        if (i == 0) {
                                            if (isExcel) {
                                                expressao = "$palavra<br><br>"
                                                significado = "$portugues<br><br>"
                                            } else expressao = "$palavra\n\n***\n\n"
                                        }
                                    }
                                    if (DESATIVAR)
                                        return null
                                }
                                if (isExcel) {
                                    expressao = expressao.substring(0, expressao.lastIndexOf("<br><br>"))
                                    significado = significado.substring(0, significado.lastIndexOf("<br><br>"))
                                }
                                TODO("Ver as tring de \\n pois ao converter fica no treim indent")

                                vocabulario += if (isExcel)
                                    "$palavra;$ingles;$portugues;$expressao;$significado;${if (leitura.isEmpty()) palavra else "$palavra[$leitura]"};$links"
                                else
                                    "$palavra $ingles $portugues $expressao$significado $links ${"-".repeat(20)}"
                            }
                        }
                    }
                } catch (e: SQLException) {
                    erro = true
                    LOGGER.error(e.message, e)
                    Platform.runLater { AlertasPopup.ErroModal("Erro de conexao", e.message!!) }
                } catch (e: Exception) {
                    erro = true
                    LOGGER.error(e.message, e)
                    Platform.runLater { AlertasPopup.ErroModal("Erro ao processar o textos", e.message!!) }
                } finally {
                    Platform.runLater {
                        if (erro)
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), 1, 1, Type.ERROR)
                        else {
                            i = 1
                            max = 1
                            updateProgress(i, max)
                            atualizaBarraWindows.run()
                        }
                        controller.setVocabulario(vocabNovo)
                        controller.setTextoDestino(vocabulario)
                        controller.habilitaBotoes()
                        MenuPrincipalController.controller.getLblLog().text = ""
                    }
                    if (erro) TimeUnit.SECONDS.sleep(5)
                    Platform.runLater {
                        concluiProgresso(false)
                        processaListaNovo(true)
                    }
                }
                return null
            }
        }
        val processa = Thread(processar)
        processa.start()
    }

    @Throws(SQLException::class)
    private fun processaTokenizer(mode: Tokenizer.SplitMode, texto: String, repetido: Boolean): String {
        var processado = ""
        for (m in tokenizer!!.tokenize(mode, texto)) {
            if (m.surface().matches(pattern) && !controller.excluido.contains(m.dictionaryForm())) {
                if (!repetido && this.repetido.contains(m.dictionaryForm())) continue
                this.repetido.add(m.dictionaryForm())
                val palavra: Optional<Vocabulario> = vocabServ!!.select(m.surface(), m.dictionaryForm())
                if (palavra.isPresent) {
                    processado += (m.dictionaryForm() + " - " + palavra.get().portugues) + " "
                    if (palavra.get().formaBasica.isEmpty() || palavra.get().leitura.isEmpty()) {
                        palavra.get().formaBasica = m.dictionaryForm()
                        palavra.get().leitura = m.readingForm()
                        vocabServ!!.update(palavra.get())
                    }
                } else {
                    processado += m.dictionaryForm() + " ** "
                    if (!vocabNovo.stream().map { e -> e.vocabulario }.anyMatch { m.surface().equals(it, ignoreCase = true) })
                        vocabNovo.add(Vocabulario(m.surface(), m.dictionaryForm(), m.readingForm(), ""))
                }
            }
        }
        return processado
    }

    private fun atualizaProgresso() {
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, max, Type.NORMAL)
    }

    fun concluiProgresso(erro: Boolean) {
        if (erro)
            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), 1, 1, Type.ERROR)
        else
            TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
    }

    private fun configura() {
        setVocabularioServices(VocabularioJaponesServices())
        setKanjiServices(KanjiServices())
    }

    @Throws(SQLException::class)
    fun processa(cnt: FrasesAnkiController) {
        controller = cnt
        configura()
        when (controller.tipo) {
            Tipo.TEXTO -> processaTexto()
            Tipo.MUSICA -> processaMusica()
            Tipo.VOCABULARIO -> processaVocabulario()
            Tipo.KANJI -> processaKanji()
            else -> {}
        }
    }

    private fun setVocabularioServices(vocabServ: VocabularioJaponesServices) {
        this.vocabServ = vocabServ
    }

    private fun setKanjiServices(kanjiServ: KanjiServices) {
        this.kanjiServ = kanjiServ
    }

    fun corrigirLancados(cnt: FrasesAnkiController) {
        controller = cnt
        vocabServ = VocabularioJaponesServices()
        val lista: List<Vocabulario> = try {
            vocabServ!!.selectAll()
        } catch (e1: SQLException) {
            e1.printStackTrace()
            return
        }
        try {
            DictionaryFactory().create(
                "", readAll(
                    FileInputStream(getPathSettings(MenuPrincipalController.controller.dicionario))
                )
            ).use { dict ->
                tokenizer = dict.create()
                val mode = getModo(MenuPrincipalController.controller.modo)
                for (vocabulario in lista) {
                    for (mp in tokenizer!!.tokenize(mode, vocabulario.vocabulario))
                        if (mp.dictionaryForm().equals(vocabulario.vocabulario, true)) {
                        vocabulario.formaBasica = mp.dictionaryForm()
                        vocabulario.leitura = mp.readingForm()
                    }
                }
                vocabServ!!.insertOrUpdate(lista)
                concluiProgresso(false)
            }
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            concluiProgresso(true)
            AlertasPopup.ErroModal("Erro ao processar textos", e.message!!)
        } catch (e: SQLException) {
            concluiProgresso(true)
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal("Erro de conexao", e.message!!)
        }
    }

    fun processaListaNovo(pesquisaSite: Boolean?) {
        MenuPrincipalController.controller.setAviso("Sudachi - Processar lista de novos registros")
        val processar: Task<Void> = object : Task<Void>() {

            @Throws(IOException::class, InterruptedException::class)
            override fun call(): Void? {
                try {
                    if (DESATIVAR)
                        return null

                    val service = RevisarJaponesServices()
                    i = 0
                    max = vocabNovo.size.toLong()
                    for (item in vocabNovo) {
                        Platform.runLater {
                            MenuPrincipalController.controller.getLblLog().text = "Processando vocabulário novo " + item.vocabulario + " - " + i + " de " + max
                        }
                        updateProgress(i, max)
                        atualizaBarraWindows.run()
                        i++
                        if (item.portugues.isEmpty()) {
                            try {
                                if (service.existe(item.vocabulario)) {
                                    val revisar = service.select(item.vocabulario).get()
                                    item.ingles = revisar.ingles
                                    item.portugues = revisar.portugues
                                    continue
                                }
                            } catch (e: SQLException) {
                                LOGGER.error(e.message, e)
                            }
                            if (!pesquisaSite!!)
                                continue

                            var signficado: String = TanoshiJapanese.processa(item.formaBasica)
                            if (signficado.isEmpty()) 
                                signficado = Jisho.processa(item.formaBasica)
                            if (signficado.isEmpty()) 
                                signficado = JapanDict.processa(item.formaBasica)
                            if (signficado.isNotEmpty()) {
                                try {
                                    item.ingles = signficado
                                    item.portugues = ScriptGoogle.translate(Language.ENGLISH.sigla, Language.PORTUGUESE.sigla, signficado, google)
                                    service.insert(Revisar(item.vocabulario, item.formaBasica, item.leitura, "", item.portugues, signficado))
                                } catch (io: IOException) {
                                    item.portugues = signficado
                                    io.printStackTrace()
                                } catch (e: SQLException) {
                                    LOGGER.error(e.message, e)
                                }
                            }
                        }
                        if (DESATIVAR)
                            return null
                    }
                } finally {
                    updateProgress(i, max)
                    atualizaBarraWindows.run()
                    Platform.runLater { controller.setVocabulario(vocabNovo) }
                    TimeUnit.SECONDS.sleep(5)
                    Platform.runLater { concluiProgresso(false) }
                }
                return null
            }
        }
        val processa = Thread(processar)
        processa.start()
    }

}