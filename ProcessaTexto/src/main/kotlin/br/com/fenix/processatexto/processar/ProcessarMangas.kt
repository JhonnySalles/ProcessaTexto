package br.com.fenix.processatexto.processar

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.controller.mangas.MangasProcessarController
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.model.enums.Site
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.*
import br.com.fenix.processatexto.tokenizers.SudachiTokenizer
import br.com.fenix.processatexto.util.Utils
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.concurrent.Task
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


class ProcessarMangas(controller: MangasProcessarController) {

    private val LOGGER = LoggerFactory.getLogger(ProcessarMangas::class.java)

    private val vocabularioJaponesService = VocabularioJaponesServices()
    private val vocabularioInglesService = VocabularioInglesServices()
    private val serviceJaponesRevisar = RevisarJaponesServices()
    private val serviceInglesRevisar = RevisarInglesServices()
    private val serviceManga = MangaServices()

    private val controller: MangasProcessarController
    private val desmembra: ProcessarPalavra = ProcessarPalavra()
    private lateinit var siteDicionario: Site
    private var desativar = false
    private var traducoes: Int = 0

    init {
        this.controller = controller
    }

    private val vocabHistorico: MutableSet<VocabularioExterno> = mutableSetOf()
    private var validaHistorico: MutableSet<String> = mutableSetOf()

    fun setDesativar(desativar: Boolean) {
        this.desativar = desativar
    }

    private var V: Double = 0.0
    private var C: Double = 0.0
    private var Progress: Long = 0
    private var Size: Long = 0

    private var vocabVolume: MutableSet<VocabularioExterno> = mutableSetOf()
    private var vocabCapitulo: MutableSet<VocabularioExterno> = mutableSetOf()
    private var vocabPagina: MutableSet<VocabularioExterno> = mutableSetOf()
    private var vocabValida: MutableSet<String> = mutableSetOf()

    private val propCapitulo: DoubleProperty = SimpleDoubleProperty(.0)
    private val propVolume: DoubleProperty = SimpleDoubleProperty(.0)
    private val propTabela: DoubleProperty = SimpleDoubleProperty(.0)

    private var error: Boolean = false

    private lateinit var tokenizer: Tokenizer
    private lateinit var mode: SplitMode

    fun processarTabelasJapones(tabelas: List<MangaTabela>) {
        error = false
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Mangas - Processar vocabulário"
        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val processarTabela: Task<Void> = object : Task<Void>() {
            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    try {
                        DictionaryFactory().create(
                            "",
                            SudachiTokenizer.readAll(FileInputStream(SudachiTokenizer.getPathSettings(MenuPrincipalController.controller.dicionario)))
                        ).use { dict ->
                            tokenizer = dict.create()
                            mode = SudachiTokenizer.getModo(MenuPrincipalController.controller.modo)
                            siteDicionario = MenuPrincipalController.controller.site

                            validaHistorico = mutableSetOf()

                            propTabela.set(.0)
                            propVolume.set(.0)
                            propCapitulo.set(.0)

                            updateMessage("Calculando tempo necessário...")

                            Progress = 0
                            Size = 0

                            tabelas.stream().filter { t -> t.isProcessar }.collect(Collectors.toList())
                                .forEach { tabela ->
                                    tabela.volumes.stream().filter { v -> v.isProcessar }
                                        .collect(Collectors.toList())
                                        .forEach { volume ->
                                            volume.capitulos.stream().filter { c -> c.isProcessar }
                                                .collect(Collectors.toList())
                                                .forEach { capitulo ->
                                                    capitulo.paginas.stream()
                                                        .filter { p -> p.isProcessar }.collect(Collectors.toList())
                                                        .forEach { pagina -> pagina.textos.forEach { texto -> if (texto.isProcessar) Size++ } }
                                                }
                                        }
                                }
                            updateMessage("Iniciando...")
                            desativar = false
                            for (tabela in tabelas) {
                                if (!tabela.isProcessar)
                                    continue
                                V = 0.0
                                for (volume in tabela.volumes) {
                                    V++
                                    if (!volume.isProcessar || volume.lingua != Language.JAPANESE) {
                                        propVolume.set(V / tabela.volumes.size)
                                        if (!volume.isProcessar)
                                            updateMessage("IGNORADO - Manga: " + volume.manga) else updateMessage("IGNORADO - Linguagem: " + volume.lingua)
                                        continue
                                    }
                                    vocabVolume = mutableSetOf()
                                    C = 0.0
                                    for (capitulo in volume.capitulos) {
                                        C++
                                        if (!capitulo.isProcessar) {
                                            updateMessage("IGNORADO - Manga: " + volume.manga + " - Capitulo " + capitulo.capitulo)
                                            propCapitulo.set(C / volume.capitulos.size)
                                            continue
                                        }
                                        vocabCapitulo = mutableSetOf()
                                        var p = 0L
                                        for (pagina in capitulo.paginas) {
                                            p++
                                            updateProgress(p, capitulo.paginas.size.toLong())
                                            if (!pagina.isProcessar) {
                                                updateMessage(("IGNORADO - Manga: " + volume.manga + " - Capitulo: " + capitulo.capitulo) + " - Página: " + pagina.nomePagina)
                                                continue
                                            }
                                            updateMessage(("Processando " + V + " de " + tabela.volumes.size + " volumes." + " Manga: " + volume.manga + " - Capitulo: " + capitulo.capitulo) + " - Página: " + pagina.nomePagina)
                                            vocabPagina = mutableSetOf()
                                            vocabValida = mutableSetOf()
                                            for (texto in pagina.textos)
                                                gerarVocabulario(texto.texto)

                                            pagina.vocabularios = vocabPagina
                                            serviceManga.updateVocabularioPagina(tabela.base, pagina)

                                            if (desativar)
                                                break
                                        }
                                        capitulo.vocabularios = vocabCapitulo
                                        serviceManga.updateVocabularioCapitulo(tabela.base, capitulo)
                                        propCapitulo.set(C / volume.capitulos.size)
                                        if (desativar)
                                            break
                                    }
                                    volume.vocabularios = vocabVolume
                                    volume.processado = true
                                    serviceManga.updateVocabularioVolume(tabela.base, volume)
                                    propVolume.set(V / tabela.volumes.size)
                                    if (desativar) {
                                        updateMessage("Revertendo a ultima alteração do Manga: " + volume.manga + " - Volume: " + volume.volume.toString())
                                        serviceManga.updateCancel(tabela.base, volume)
                                        break
                                    }
                                }
                                if (desativar)
                                    break
                            }
                        }
                    } catch (e: IOException) {
                        LOGGER.error(e.message, e)
                        error = true
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    error = true
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                if (error)
                    AlertasPopup.ErroModal(controller.controllerPai.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar os mangas.")
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.controllerPai.stackPane, controller.root, mutableListOf(), "Aviso", "Mangas processadas com sucesso.")

                progress.barraProgresso.progressProperty().unbind()
                controller.barraProgressoVolumes.progressProperty().unbind()
                controller.barraProgressoCapitulos.progressProperty().unbind()
                controller.barraProgressoPaginas.progressProperty().unbind()
                progress.log.textProperty().unbind()
                controller.habilitar()

                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de processamento da tabela: " + super.getMessage())
                print("Erro na thread de processamento da tabela: " + super.getMessage())
            }
        }

        progress.barraProgresso.progressProperty().bind(propTabela)
        controller.barraProgressoVolumes.progressProperty().bind(propVolume)
        controller.barraProgressoCapitulos.progressProperty().bind(propCapitulo)
        controller.barraProgressoPaginas.progressProperty().bind(processarTabela.progressProperty())
        progress.log.textProperty().bind(processarTabela.messageProperty())
        val t = Thread(processarTabela)
        t.start()
    }

    private fun getSignificado(kanji: String): String {
        if (kanji.trim().isEmpty()) return ""
        Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$kanji : Obtendo significado." }
        var resultado = ""
        when (siteDicionario) {
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
        var resultado: String
        Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$palavra : Desmembrando a palavra." }
        resultado = processaPalavras(palavra, desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.B), Modo.B)
        if (resultado.isEmpty()) resultado =
            processaPalavras(palavra, desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)
        return resultado
    }

    private fun processaPalavras(original: String, palavras: List<String>, modo: Modo): String {
        var desmembrado = ""
        try {
            for (palavra in palavras) {
                if (original.equals(palavra, ignoreCase = true))
                    continue

                var resultado = getSignificado(palavra)
                if (resultado.trim().isNotEmpty())
                    desmembrado += "$palavra - $resultado; "
                else if (modo == Modo.B) {
                    resultado = processaPalavras(original, desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)
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

    private val pattern = ".*[\u4E00-\u9FAF].*".toRegex()
    private val japanese = ".*[\u3041-\u9FAF].*".toRegex()

    @Throws(SQLException::class)
    private fun gerarVocabulario(frase: String) {
        for (m in tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                if (validaHistorico.contains(m.dictionaryForm())) {
                    val vocabulario: VocabularioExterno = vocabHistorico.stream()
                        .filter { vocab -> m.dictionaryForm().equals(vocab.palavra, true) }
                        .findFirst().orElse(null)

                    if (vocabulario != null) {
                        vocabPagina.add(vocabulario)
                        vocabCapitulo.add(vocabulario)
                        vocabVolume.add(vocabulario)
                        continue
                    }
                }
                if (!vocabValida.contains(m.dictionaryForm())) {
                    val palavra = vocabularioJaponesService.select(m.surface(), m.dictionaryForm()).orElse(null)
                    if (palavra != null) {
                        val vocabulario = VocabularioExterno(palavra.getId(), palavra.vocabulario, palavra.portugues, palavra.ingles, palavra.leitura, true)

                        // Usado apenas para correção em formas em branco.
                        if (palavra.formaBasica.isEmpty()) {
                            palavra.formaBasica = m.dictionaryForm()
                            palavra.leitura = m.readingForm()
                            vocabularioJaponesService.update(palavra)
                        }

                        validaHistorico.add(m.dictionaryForm())
                        vocabHistorico.add(vocabulario)
                        vocabValida.add(m.dictionaryForm())
                        vocabPagina.add(vocabulario)
                        vocabCapitulo.add(vocabulario)
                        vocabVolume.add(vocabulario)
                    } else {
                        var revisar = serviceJaponesRevisar.select(m.surface(), m.dictionaryForm()).orElse(null)
                        if (revisar == null) {
                            revisar = Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), "", revisado = false, isAnime = false, isManga = true, isNovel = false)
                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = m.surface() + " : Vocabulário novo." }
                            revisar.ingles = getSignificado(revisar.vocabulario)

                            if (revisar.ingles.isEmpty() && !revisar.formaBasica.equals(revisar.vocabulario, ignoreCase = true))
                                revisar.ingles = getSignificado(revisar.formaBasica)

                            if (revisar.ingles.isEmpty())
                                revisar.ingles = getDesmembrado(revisar.vocabulario)

                            if (revisar.ingles.isNotEmpty()) {
                                try {
                                    traducoes++
                                    if (traducoes > 3000) {
                                        traducoes = 0
                                        MenuPrincipalController.controller.contaGoogle = Utils.next(MenuPrincipalController.controller.contaGoogle)
                                    }

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
                            serviceJaponesRevisar.insert(revisar)
                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "" }
                        } else {
                            if (!revisar.isManga) {
                                revisar.isManga = true
                                serviceJaponesRevisar.setIsManga(revisar)
                            }
                            serviceJaponesRevisar.incrementaVezesAparece(revisar.vocabulario)
                        }
                        val vocabulario = VocabularioExterno(revisar.getId(), revisar.vocabulario, revisar.portugues, revisar.ingles, m.readingForm(), false)

                        validaHistorico.add(m.dictionaryForm())
                        vocabHistorico.add(vocabulario)
                        vocabValida.add(m.dictionaryForm())
                        vocabPagina.add(vocabulario)
                        vocabCapitulo.add(vocabulario)
                        vocabVolume.add(vocabulario)
                    }
                }
            }
        }
        Progress++
        propTabela.set(Progress.toDouble() / Size)
        Platform.runLater { if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, Type.NORMAL) }
    }

    private var palavraValida: MutableSet<String> = mutableSetOf()

    fun processarTabelasIngles(tabelas: List<MangaTabela>) {
        error = false
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Mangas - Processar vocabulário"
        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val processarTabela: Task<Void> = object : Task<Void>() {
            val matcher: Pattern = Pattern.compile("[\\d|\\W]")

            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    propTabela.set(.0)
                    propVolume.set(.0)
                    propCapitulo.set(.0)
                    validaHistorico = mutableSetOf()
                    updateMessage("Calculando tempo necessário...")
                    Progress = 0
                    Size = 0
                    tabelas.stream().filter { t -> t.isProcessar }.collect(Collectors.toList())
                        .forEach { tabela ->
                            tabela.volumes.stream().filter { v -> v.isProcessar }
                                .collect(Collectors.toList())
                                .forEach { volume ->
                                    volume.capitulos.stream().filter { c -> c.isProcessar }
                                        .collect(Collectors.toList())
                                        .forEach { capitulo ->
                                            capitulo.paginas.stream()
                                                .filter { p -> p.isProcessar }.collect(Collectors.toList())
                                                .forEach { pagina -> pagina.textos.forEach { texto -> if (texto.isProcessar) Size++ } }
                                        }
                                }
                        }
                    updateMessage("Iniciando...")
                    desativar = false
                    for (tabela in tabelas) {
                        if (!tabela.isProcessar) continue
                        palavraValida = mutableSetOf()
                        V = 0.0
                        for (volume in tabela.volumes) {
                            V++
                            if (!volume.isProcessar || volume.lingua != Language.ENGLISH) {
                                propVolume.set(V / tabela.volumes.size)
                                if (!volume.isProcessar)
                                    updateMessage("IGNORADO - Manga: " + volume.manga)
                                else
                                    updateMessage("IGNORADO - Linguagem: " + volume.lingua)
                                continue
                            }
                            vocabVolume = mutableSetOf()
                            C = 0.0
                            for (capitulo in volume.capitulos) {
                                C++
                                if (!capitulo.isProcessar) {
                                    updateMessage("IGNORADO - Manga: " + volume.manga + " - Capitulo " + capitulo.capitulo)
                                    propCapitulo.set(C / volume.capitulos.size)
                                    continue
                                }
                                vocabCapitulo = mutableSetOf()
                                var p = 0L
                                for (pagina in capitulo.paginas) {
                                    p++
                                    updateProgress(p, capitulo.paginas.size.toLong())
                                    if (!pagina.isProcessar) {
                                        updateMessage(("IGNORADO - Manga: " + volume.manga + " - Capitulo: " + capitulo.capitulo) + " - Página: " + pagina.nomePagina)
                                        continue
                                    }
                                    updateMessage(("Processando " + V + " de " + tabela.volumes.size + " volumes." + " Manga: " + volume.manga) + " - Capitulo: " + capitulo.capitulo + " - Página: " + pagina.nomePagina)
                                    vocabPagina = mutableSetOf()
                                    vocabValida = mutableSetOf()
                                    for (texto in pagina.textos) {
                                        if (texto.texto.isNotEmpty()) {
                                            val palavras: Set<String> = texto.texto.lowercase(Locale.getDefault())
                                                .split(" ")
                                                .map { txt -> txt.replace("\\W".toRegex(), "") }
                                                .filter { txt -> !txt.trim().contains(" ") && txt.isNotEmpty() }
                                                .distinct().toSet()

                                            for (palavra in palavras) {
                                                if (matcher.matcher(palavra).find())
                                                    continue

                                                if (validaHistorico.contains(palavra)) {
                                                    val vocabulario: VocabularioExterno? = vocabHistorico.stream()
                                                        .filter { vocab -> palavra.equals(vocab.palavra, true) }
                                                        .findFirst().orElse(null)
                                                    if (vocabulario != null) {
                                                        vocabPagina.add(vocabulario)
                                                        vocabCapitulo.add(vocabulario)
                                                        vocabVolume.add(vocabulario)
                                                        continue
                                                    }
                                                }

                                                if (!vocabValida.contains(palavra)) {
                                                    val salvo = vocabularioInglesService.select(palavra).orElse(null)
                                                    if (salvo != null) {
                                                        val vocabulario = VocabularioExterno(salvo.getId(), palavra, salvo.portugues, true)
                                                        validaHistorico.add(palavra)
                                                        vocabHistorico.add(vocabulario)
                                                        vocabValida.add(palavra)
                                                        vocabPagina.add(vocabulario)
                                                        vocabCapitulo.add(vocabulario)
                                                        vocabVolume.add(vocabulario)
                                                    } else {
                                                        if (!palavraValida.contains(palavra.lowercase(Locale.getDefault()))) {
                                                            val valido: String = serviceInglesRevisar.isValido(palavra)
                                                            if (valido.isEmpty())
                                                                continue
                                                            palavraValida.add(valido)
                                                        }

                                                        var revisar: Revisar? = serviceInglesRevisar.select(palavra).orElse(null)
                                                        if (revisar == null) {
                                                            revisar = Revisar(palavra, revisado = false, isAnime = false, isManga = true, isNovel = false)
                                                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$palavra : Vocabulário novo." }

                                                            if (revisar.vocabulario.isNotEmpty()) {
                                                                try {
                                                                    traducoes++
                                                                    if (traducoes > 3000) {
                                                                        traducoes = 0
                                                                        MenuPrincipalController.controller.contaGoogle = Utils.next(MenuPrincipalController.controller.contaGoogle)
                                                                    }
                                                                    Platform.runLater {
                                                                        MenuPrincipalController.controller.getLblLog().text = "$palavra : Obtendo tradução."
                                                                    }
                                                                    revisar.portugues = Utils.normalize(
                                                                        ScriptGoogle.translate(
                                                                            Language.ENGLISH.sigla,
                                                                            Language.PORTUGUESE.sigla,
                                                                            revisar.vocabulario,
                                                                            MenuPrincipalController.controller.contaGoogle
                                                                        )
                                                                    )
                                                                } catch (e: IOException) {
                                                                    LOGGER.error(e.message, e)
                                                                }
                                                            }
                                                            serviceInglesRevisar.insert(revisar)
                                                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "" }
                                                        } else {
                                                            if (!revisar.isManga) {
                                                                revisar.isManga = true
                                                                serviceInglesRevisar.setIsManga(revisar)
                                                            }
                                                            serviceInglesRevisar.incrementaVezesAparece(revisar.vocabulario)
                                                        }

                                                        val vocabulario = VocabularioExterno(revisar.getId(), palavra, revisar.portugues, false)

                                                        validaHistorico.add(palavra)
                                                        vocabHistorico.add(vocabulario)
                                                        vocabValida.add(palavra)
                                                        vocabPagina.add(vocabulario)
                                                        vocabCapitulo.add(vocabulario)
                                                        vocabVolume.add(vocabulario)
                                                    }
                                                }
                                            }
                                        }
                                        Progress++
                                        propTabela.set(Progress.toDouble() / Size)
                                        Platform.runLater {
                                            if (TaskbarProgressbar.isSupported())
                                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, Type.NORMAL)
                                        }
                                    }
                                    pagina.vocabularios = vocabPagina
                                    serviceManga.updateVocabularioPagina(tabela.base, pagina)

                                    if (desativar)
                                        break
                                }
                                capitulo.vocabularios = vocabCapitulo
                                serviceManga.updateVocabularioCapitulo(tabela.base, capitulo)
                                propCapitulo.set(C / volume.capitulos.size)
                                if (desativar)
                                    break
                            }
                            volume.vocabularios = vocabVolume
                            volume.processado = true

                            serviceManga.updateVocabularioVolume(tabela.base, volume)
                            propVolume.set(V / tabela.volumes.size)
                            if (desativar) {
                                updateMessage("Revertendo a ultima alteração do Manga: " + volume.manga + " - Volume: " + volume.volume.toString())
                                serviceManga.updateCancel(tabela.base, volume)
                                break
                            }
                        }
                        if (desativar)
                            break
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    error = true
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                if (error)
                    AlertasPopup.ErroModal(controller.controllerPai.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar os mangas.")
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.controllerPai.stackPane, controller.root, mutableListOf(), "Aviso", "Mangas processadas com sucesso.")

                progress.barraProgresso.progressProperty().unbind()
                controller.barraProgressoVolumes.progressProperty().unbind()
                controller.barraProgressoCapitulos.progressProperty().unbind()
                controller.barraProgressoPaginas.progressProperty().unbind()
                progress.log.textProperty().unbind()
                controller.habilitar()

                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de processamento da tabela: " + super.getMessage())
                print("Erro na thread de processamento da tabela: " + super.getMessage())
            }
        }

        progress.barraProgresso.progressProperty().bind(propTabela)
        controller.barraProgressoVolumes.progressProperty().bind(propVolume)
        controller.barraProgressoCapitulos.progressProperty().bind(propCapitulo)
        controller.barraProgressoPaginas.progressProperty().bind(processarTabela.progressProperty())
        progress.log.textProperty().bind(processarTabela.messageProperty())
        val t = Thread(processarTabela)
        t.start()
    }
}