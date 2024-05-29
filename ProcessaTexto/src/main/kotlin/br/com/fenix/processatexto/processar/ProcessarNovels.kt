package br.com.fenix.processatexto.processar

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.controller.BaseController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.controller.novels.NovelsImportarController
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapitulo
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Dicionario
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.model.enums.Site
import br.com.fenix.processatexto.processar.scriptGoogle.ScriptGoogle
import br.com.fenix.processatexto.service.*
import br.com.fenix.processatexto.tokenizers.SudachiTokenizer
import br.com.fenix.processatexto.util.Utils
import com.google.common.io.Files
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.concurrent.Task
import javafx.util.Callback
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapa
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela
import br.com.fenix.processatexto.model.entities.novelextractor.NovelTexto
import br.com.fenix.processatexto.model.entities.novelextractor.NovelVolume
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.awt.image.BufferedImage
import java.io.*
import java.sql.SQLException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.imageio.ImageIO
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class ProcessarNovels(controller: BaseController) {

    private val LOGGER: Logger = LoggerFactory.getLogger(ProcessarNovels::class.java)

    private val LOGFILE = "log.txt"

    private val vocabularioJaponesService = VocabularioJaponesServices()
    private val vocabularioInglesService = VocabularioInglesServices()
    private val serviceJaponesRevisar = RevisarJaponesServices()
    private val serviceInglesRevisar = RevisarInglesServices()
    private val serviceNovel = NovelServices()

    private val controller: BaseController
    private val desmembra: ProcessarPalavra = ProcessarPalavra()

    private lateinit var siteDicionario: Site
    private var desativar = false
    private var traducoes: Int = 0
    private val vocabHistorico: MutableSet<VocabularioExterno> = mutableSetOf()
    private var validaHistorico: MutableSet<String> = mutableSetOf()

    fun setDesativar(desativar: Boolean) {
        this.desativar = desativar
    }

    init {
        this.controller = controller
    }

    private var V: Long = 0
    private var Progress: Long = 0
    private var Size: Long = 0
    private var mensagem: String = ""

    private var vocabVolume: MutableSet<VocabularioExterno> = mutableSetOf()
    private var vocabCapitulo: MutableSet<VocabularioExterno> = mutableSetOf()
    private var vocabValida: MutableSet<String> = mutableSetOf()
    private val vocabErros: MutableMap<String, Int> = mutableMapOf()

    private val propTexto = SimpleDoubleProperty(.0)
    private var error: Boolean = false

    private lateinit var tokenizer: Tokenizer
    private lateinit var mode: SplitMode

    private fun toAlfabeto(texto: String): String {
        var tabela = "temp"
        when (texto) {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> tabela = "0"
            "あ", "ア", "A", "a" -> tabela = "a"
            "え", "エ", "E", "e" -> tabela = "e"
            "い", "イ", "I", "i" -> tabela = "i"
            "お", "オ", "O", "o" -> tabela = "o"
            "う", "ウ", "U", "u" -> tabela = "u"
            "ば", "べ", "び", "ぼ", "バ", "ベ", "ビ", "ボ", "B", "b" -> tabela = "b"
            "ち", "チ", "C", "c" -> tabela = "c"
            "だ", "で", "ど", "ダ", "デ", "ド", "D", "d" -> tabela = "d"
            "ふ", "フ", "ぶ", "ブ", "F", "f" -> tabela = "f"
            "G", "g" -> tabela = "g"
            "は", "へ", "ひ", "ほ", "ハ", "ヘ", "ヒ", "ホ", "H", "h" -> tabela = "h"
            "じ", "ジ", "J", "j" -> tabela = "j"
            "か", "け", "き", "こ", "く", "が", "げ", "ぎ", "ご", "ぐ", "カ", "ケ", "キ", "コ", "ク", "ガ", "ゲ", "ギ", "ゴ", "グ", "K", "k" -> tabela = "k"
            "L", "l" -> tabela = "l"
            "ま", "め", "み", "も", "む", "マ", "メ", "ミ", "モ", "ム", "M", "m" -> tabela = "m"
            "な", "ね", "に", "の", "ぬ", "ん", "ナ", "ネ", "ニ", "ノ", "ヌ", "ン", "N", "n" -> tabela = "n"
            "ぱ", "ぺ", "ぴ", "ぽ", "ぷ", "パ", "ペ", "ピ", "ポ", "プ", "P", "p" -> tabela = "p"
            "Q", "q" -> tabela = "q"
            "ら", "れ", "り", "ろ", "る", "ラ", "レ", "リ", "ロ", "ル", "R", "r" -> tabela = "r"
            "さ", "せ", "し", "そ", "す", "サ", "セ", "シ", "ソ", "ス", "S", "s" -> tabela = "s"
            "た", "て", "と", "つ", "づ", "タ", "テ", "ト", "ツ", "ヅ", "T", "t" -> tabela = "t"
            "V", "v" -> tabela = "v"
            "や", "よ", "ゆ", "ヤ", "ヨ", "ユ", "Y", "y" -> tabela = "y"
            "わ", "を", "ワ", "ヲ", "W", "w" -> tabela = "w"
            "X", "x" -> tabela = "x"
            "ざ", "ぜ", "ぞ", "ず", "ザ", "ゼ", "ゾ", "ズ", "Z", "z" -> tabela = "z"
        }
        return tabela
    }

    private fun getBase(texto: String, inicio: Int): Int {
        var base = 1
        for (n in inicio until texto.length) {
            when (texto.substring(n, n + 1)) {
                "十" -> if (base < 10) base = 10
                "百" -> if (base < 100) base = 100
                "千" -> if (base < 1000) base = 1000
                "万" -> base *= 10000
                "億" -> base *= 100000000
            }
        }
        return base
    }

    private fun toNumero(texto: String, original: Float): Float {
        var numero = original
        if (texto.isNotEmpty()) {
            if (texto.matches("([０-９])*".toRegex()))
                numero = texto.replace("\uFF10".toRegex(), "0").replace("\uFF11".toRegex(), "1").replace("\uFF12".toRegex(), "2").replace("\uFF13".toRegex(), "3")
                    .replace("\uFF14".toRegex(), "4").replace("\uFF15".toRegex(), "5").replace("\uFF16".toRegex(), "6").replace("\uFF17".toRegex(), "7")
                    .replace("\uFF18".toRegex(), "8").replace("\uFF19".toRegex(), "9").toFloat()
            else {
                var num = 0f
                var base = 1
                for (n in texto.length - 1 downTo 0) {
                    when (texto.substring(n, n + 1)) {
                        "零" -> num += 0f * base
                        "一" -> num += 1f * base
                        "二" -> num += 2f * base
                        "三" -> num += 3f * base
                        "四" -> num += 4f * base
                        "五" -> num += 5f * base
                        "六" -> num += 6f * base
                        "七" -> num += 7f * base
                        "八" -> num += 8f * base
                        "九" -> num += 9f * base
                        "十", "百", "千", "万", "億" -> base = getBase(texto, n)
                    }
                }
                if (num > numero)
                    numero = num
            }
        }
        return numero
    }

    private fun getBase(linguagem: Language, texto: String): String {
        val tabela: String
        var nome = ""
        if (linguagem == Language.JAPANESE) {
            var matcher: Matcher = Pattern.compile("^[a-zA-Z0-9]").matcher(texto)
            if (matcher.find() && matcher.group(0).isNotEmpty())
                tabela = toAlfabeto(matcher.group(0).substring(0, 1))
            else {
                matcher = Pattern.compile("([\u3041-\u9FAF]+)").matcher(texto)
                if (matcher.find() && matcher.group(0).isNotEmpty()) {
                    val item: String = matcher.group(0)
                    if (item.trim().substring(0, 1).matches("[ぁ-んァ-ンa-zA-Z0-9]".toRegex()))
                        tabela = toAlfabeto(item.trim().substring(0, 1))
                    else {
                        val m: List<Morpheme> = tokenizer.tokenize(SplitMode.A, item)
                        if (m.isNotEmpty()) {
                            if (m[0].readingForm().isNotEmpty())
                                nome = m[0].readingForm().substring(0, 1)
                            else if (m[0].surface().isNotEmpty())
                                nome = m[0].surface().substring(0, 1)
                        }
                        tabela = toAlfabeto(nome)
                    }
                } else tabela = texto.substring(0, 1)
            }
        } else tabela = texto.substring(0, 1)
        return tabela
    }

    @Throws(IOException::class)
    private fun getVolume(arquivo: File, linguagem: Language, favorito: Boolean): NovelVolume {
        val jpg = File(arquivo.path.substring(0, arquivo.path.lastIndexOf(".")) + ".jpg")
        val opf = File(arquivo.path.substring(0, arquivo.path.lastIndexOf(".")) + ".opf")
        val arq: String = arquivo.name.substring(0, arquivo.name.lastIndexOf("."))

        val nome: String = if (arq.lowercase(Locale.getDefault()).contains("volume"))
            arq.substring(0, arq.lowercase(Locale.getDefault()).lastIndexOf("volume")).trim()
        else if (arq.lowercase(Locale.getDefault()).contains("vol."))
            arq.substring(0, arq.lowercase(Locale.getDefault()).lastIndexOf("vol.")).trim()
        else arq

        var titulo = ""
        if (nome.matches("[a-zA-Z\\d]".toRegex()))
            titulo = nome

        var volume = 0f
        var matcher: Matcher = Pattern.compile("((volume |vol. |vol )?([\\d]+.)?[\\d]+)").matcher(arq.lowercase(Locale.getDefault()))
        if (matcher.find() && matcher.group(0).isNotEmpty()) {
            val aux: String = matcher.group(0).lowercase(Locale.getDefault()).replace("volume", "").replace("vol.", "").replace("vol", "").trim()
            if (aux.matches("[\\d.]+".toRegex()))
                volume = aux.toFloat()
        }

        if (volume <= 0f) {
            matcher = Pattern.compile("(([0-9]+.)?[0-9]+$)").matcher(arq.trim())
            if (matcher.find() && matcher.group(0).isNotEmpty()) {
                volume = if (matcher.group(0).contains(" "))
                    matcher.group(0).substring(matcher.group(0).lastIndexOf(" ")).trim().toFloat()
                else
                    matcher.group(0).toFloat()

                if (nome.matches("[a-zA-Z\\d]".toRegex()) && nome.contains(matcher.group(0)))
                    titulo = nome.substring(0, nome.lastIndexOf(matcher.group(0)))
            } else {
                matcher = Pattern.compile("(([0-9]+.)?[0-9]+)").matcher(arq.trim())
                if (matcher.find() && matcher.group(0).isNotEmpty() && !matcher.group(0).contains("-"))
                    volume = matcher.group(0).toFloat()
                else {
                    matcher = Pattern.compile("(([\uFF10-\uFF19]+.)?[\uFF10-\uFF19]+$)").matcher(arq.trim())
                    if (matcher.find() && matcher.group(0).isNotEmpty())
                        volume =
                            matcher.group(0).replace("\uFF10".toRegex(), "0").replace("\uFF11".toRegex(), "1").replace("\uFF12".toRegex(), "2").replace("\uFF13".toRegex(), "3")
                                .replace("\uFF14".toRegex(), "4").replace("\uFF15".toRegex(), "5").replace("\uFF16".toRegex(), "6").replace("\uFF17".toRegex(), "7")
                                .replace("\uFF18".toRegex(), "8").replace("\uFF19".toRegex(), "9").toFloat()
                    else {
                        matcher = Pattern.compile("(([\uFF10-\uFF19]+.)?[\uFF10-\uFF19]+)").matcher(arq.trim())
                        if (matcher.find() && matcher.group(0).isNotEmpty())
                            volume =
                                matcher.group(0).replace("\uFF10".toRegex(), "0").replace("\uFF11".toRegex(), "1").replace("\uFF12".toRegex(), "2").replace("\uFF13".toRegex(), "3")
                                    .replace("\uFF14".toRegex(), "4").replace("\uFF15".toRegex(), "5").replace("\uFF16".toRegex(), "6").replace("\uFF17".toRegex(), "7")
                                    .replace("\uFF18".toRegex(), "8").replace("\uFF19".toRegex(), "9").replace("\uFF0E".toRegex(), ".").toFloat()
                    }
                }
            }
        }
        val novel = NovelVolume(UUID.randomUUID(), nome, titulo, "", "", "", arquivo.name, "", "", volume, linguagem, favorito, false)
        if (jpg.exists()) {
            val imagem: BufferedImage = ImageIO.read(jpg)
            novel.capa = NovelCapa(UUID.randomUUID(), novel.novel, novel.volume, novel.lingua, jpg.name, "jpg", imagem)
        }
        if (opf.exists()) {
            try {
                val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

                // parse XML file
                val db: DocumentBuilder = dbf.newDocumentBuilder()
                val doc: Document = db.parse(opf)
                val title: NodeList = doc.getElementsByTagName("dc:title")
                if (title != null && title.length > 0)
                    novel.titulo = title.item(0).textContent
                var autor = ""
                val creator: NodeList = doc.getElementsByTagName("dc:creator")
                if (creator != null && creator.length > 0) {
                    for (i in 0 until creator.length)
                        autor += creator.item(i).textContent + "; "
                    if (creator.item(0).hasAttributes())
                        autor += " (" + creator.item(0).attributes.getNamedItem("opf:file-as").textContent + "); "
                    autor = autor.substring(0, autor.lastIndexOf("; "))
                }
                novel.autor = autor
                val publisher: NodeList = doc.getElementsByTagName("dc:publisher")
                if (publisher != null && publisher.length > 0)
                    novel.editora = publisher.item(0).textContent

                val sort: NodeList = doc.getElementsByTagName("calibre:title_sort")
                if (sort != null && sort.length > 0)
                    novel.tituloAlternativo = sort.item(0).textContent

                val description: NodeList = doc.getElementsByTagName("dc:description")
                if (description != null && description.length > 0)
                    novel.descricao = description.item(0).textContent

                val series: NodeList = doc.getElementsByTagName("calibre:serie")
                if (series != null && series.length > 0)
                    novel.serie = series.item(0).textContent
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
            }
        }
        return novel
    }

    @Throws(IOException::class)
    private fun getTabela(tabela: String?, arquivo: File, linguagem: Language, favorito: Boolean): NovelTabela {
        val list: MutableList<NovelVolume> = mutableListOf()
        val vol: NovelVolume = getVolume(arquivo, linguagem, favorito)
        list.add(vol)
        return if (tabela != null && tabela.isNotEmpty()) NovelTabela(tabela, list) else NovelTabela(getBase(linguagem, vol.novel), list)
    }

    private fun getIndices(volume: NovelVolume, textos: MutableList<NovelTexto>, linguagem: Language) {
        val indices: MutableMap<Int, String> = mutableMapOf()
        for (texto in textos) {
            if (texto.texto.contains("*"))
                indices[texto.sequencia] = texto.texto.replace("\\* ".toRegex(), "").trim()
            else if (texto.texto.lowercase(Locale.getDefault()).contains("índice:"))
                continue
            else
                break
        }
        if (indices.isNotEmpty()) {
            var lastCap = 0f
            for (k in indices.keys) {
                val indice = indices[k] ?: ""
                var cap = lastCap
                if (linguagem == Language.JAPANESE) {
                    val matcher: Matcher = Pattern.compile("((第)?([\\d]|[０-９]|零|一|二|三|四|五|六|七|八|九|十|千|万|百|億|兆)*(話|譜|章))").matcher(indice)
                    if (matcher.find() && matcher.group(0).isNotEmpty()) {
                        val aux: String = matcher.group(0).replace("(第|話|譜|章)".toRegex(), "")
                        if (aux.matches("([０-９]|零|一|二|三|四|五|六|七|八|九|十|千|万|百|億|兆)*".toRegex()))
                            cap = toNumero(aux, cap)
                        else if (aux.matches("([\\d])*".toRegex()))
                            cap = aux.toFloat()
                    }
                } else if (linguagem == Language.ENGLISH) {
                    var matcher: Matcher = Pattern.compile("((capítulo |capitulo |cap. |cap )?([\\d.]+)?[\\d]+)").matcher(indice.lowercase(Locale.getDefault()))
                    if (matcher.find() && matcher.group(0).isNotEmpty()) {
                        val aux: String = matcher.group(0).lowercase(Locale.getDefault())
                            .replace("capítulo", "")
                            .replace("capitulo", "")
                            .replace("cap.", "")
                            .replace("cap", "").trim()
                        matcher = Pattern.compile("(\\d*\\.?\\d+)").matcher(aux)
                        if (matcher.find() && matcher.group(0).isNotEmpty())
                            cap = matcher.group(0).toFloat()
                    }
                }
                if (cap === lastCap) {
                    val matcher: Matcher = Pattern.compile("^(([\\d.]+)?[\\d]+)").matcher(indice.lowercase(Locale.getDefault()))
                    if (matcher.find() && matcher.group(0).isNotEmpty())
                        cap = matcher.group(0).toFloat()
                }
                val capitulo = NovelCapitulo(UUID.randomUUID(), volume.novel, volume.volume, cap, indice, k, volume.lingua)
                var pi = 0
                var pos = -1
                for (i in pi until textos.size) {
                    pi = i
                    if (textos[i].texto.trim().equals(indice, ignoreCase = true)) {
                        pi = i + 1
                        pos = i
                        break
                    }
                }
                for (i in pi until textos.size) {
                    if (textos[i].texto.trim().equals(indice, ignoreCase = true)) {
                        pos = i
                        break
                    }
                }
                if (pos >= 0) {
                    capitulo.sequencia = textos[0].sequencia
                    for (i in 0..pos)
                        capitulo.addTexto(textos.removeFirst())
                }
                lastCap = cap
                volume.addCapitulos(capitulo)
            }
            if (textos.isNotEmpty()) {
                val capitulo: NovelCapitulo = volume.capitulos.last()
                for (i in 0 until textos.size)
                    capitulo.addTexto(textos.removeFirst())
            }
        } else {
            val capitulo = NovelCapitulo(UUID.randomUUID(), volume.novel, volume.volume, 0f, "", 0, volume.lingua)
            capitulo.textos = textos
            volume.addCapitulos(capitulo)
        }
    }

    private fun addLog(texto: String) {
        if (controller != null && controller is NovelsImportarController)
            Platform.runLater { controller.addLog(texto) }

        if (LOG != null && LOG!!.exists())
            try {
                BufferedWriter(FileWriter(LOG, true)).use { writer ->
                    writer.append(texto)
                    writer.newLine()
                    writer.flush()
                }
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
            }
    }

    private var LOG: File? = null
    fun processarArquivos(caminho: File, tabela: String?, linguagem: Language, favorito: Boolean) {
        error = false
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Novels - Processar arquivos"
        val processarArquivos: Task<Void> = object : Task<Void>() {

            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                LOG = if (caminho.isDirectory)
                    File(caminho.toString() + "\\" + LOGFILE)
                else
                    File(caminho.path.substring(0, caminho.path.lastIndexOf("\\")) + "\\" + LOGFILE)

                val concluido = File("$caminho\\concluido\\")
                if (!concluido.exists()) concluido.mkdirs()
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
                            propTexto.set(.0)

                            addLog("Preparando arquivos...")
                            val arquivos: MutableMap<String, File> = mutableMapOf()
                            val novels: MutableList<NovelTabela> = mutableListOf()

                            if (caminho.isDirectory) {
                                for (arquivo in caminho.listFiles()!!)
                                    if (!arquivo.name.equals(LOGFILE, true) && arquivo.name.substring(arquivo.name.lastIndexOf('.') + 1).equals("txt", true)) {
                                        addLog("Preparando " + arquivo.name)

                                        val obj: NovelTabela = getTabela(tabela, arquivo, linguagem, favorito)
                                        val tab: Optional<NovelTabela> = novels.stream().filter { i -> i.base.equals(obj.base, true) }.findFirst()
                                        if (tab.isPresent)
                                            tab.get().volumes.addAll(obj.volumes)
                                        else
                                            novels.add(obj)
                                        arquivos[arquivo.name] = arquivo
                                    }
                            } else {
                                novels.add(getTabela(tabela, caminho, linguagem, favorito))
                                arquivos[caminho.name] = caminho
                            }

                            Progress = 0
                            Size = 0
                            novels.forEach { t -> Size += t.volumes.size }
                            Size *= 3
                            desativar = false
                            for (novel in novels) {
                                for (volume in novel.volumes) {
                                    propTexto.set(.0)
                                    updateMessage("Importando texto do arquivo " + volume.arquivo + "...")
                                    updateProgress(++Progress, Size)
                                    addLog("Importando texto do arquivo " + volume.arquivo + "...")
                                    addLog("Inicio do processo: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))

                                    val textos: MutableList<NovelTexto> = mutableListOf()
                                    val fr = FileReader(arquivos[volume.arquivo])
                                    BufferedReader(fr).use { br ->
                                        var seq = 0
                                        var line: String
                                        while (br.readLine().also { line = it ?: "" } != null) {
                                            if (line.trim().isEmpty()) continue
                                            seq++
                                            textos.add(NovelTexto(UUID.randomUUID(), line, seq))
                                        }
                                    }
                                    getIndices(volume, textos, linguagem)
                                    if (desativar)
                                        break
                                    addLog("Processando textos... ")
                                    updateMessage("Processando textos... ")
                                    updateProgress(++Progress, Size)
                                    val callback: Callback<Array<Int>, Boolean> = Callback<Array<Int>, Boolean> { param ->
                                        Platform.runLater {
                                            updateMessage("Processando itens...." + param[0] + '/' + param[1])
                                            propTexto.set(param[0].toDouble() / param[1])
                                        }
                                        true
                                    }
                                    when (linguagem) {
                                        Language.JAPANESE -> processarJapones(volume, callback)
                                        Language.ENGLISH -> processarIngles(volume, callback)
                                        else -> {}
                                    }
                                    if (desativar)
                                        break

                                    updateMessage("Salvando textos...")
                                    updateProgress(++Progress, Size)
                                    addLog("Salvando textos...")
                                    serviceNovel.salvarVolume(novel.base, volume)
                                    addLog("Concluído processamento do arquivo " + volume.arquivo + ".")
                                    addLog("-".repeat(30))
                                    addLog("")

                                    val arq: File = arquivos[volume.arquivo]!!
                                    val jpg = File(arq.path.substring(0, arq.path.lastIndexOf(".")) + ".jpg")
                                    val opf = File(arq.path.substring(0, arq.path.lastIndexOf(".")) + ".opf")

                                    Files.move(arq, File(concluido, volume.arquivo))
                                    if (jpg.exists())
                                        Files.move(jpg, File(concluido, jpg.name))

                                    if (opf.exists())
                                        Files.move(opf, File(concluido, opf.name))
                                    Platform.runLater {
                                        if (TaskbarProgressbar.isSupported())
                                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.NORMAL)
                                    }
                                }
                                if (desativar)
                                    break
                            }
                            Platform.runLater {
                                if (TaskbarProgressbar.isSupported())
                                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                            }
                        }
                    } catch (e: IOException) {
                        LOGGER.error(e.message, e)
                        error = true
                        addLog("Erro ao processar o arquivo.")
                        addLog(e.message!!)
                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR)
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    error = true
                    addLog("Erro ao processar o arquivo.")
                    addLog(e.message!!)
                    Platform.runLater {
                        if (TaskbarProgressbar.isSupported())
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR)
                    }
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                if (error)
                    AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar as novels.")
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.stackPane, controller.root, mutableListOf(), "Aviso", "Novels processadas com sucesso.")

                if (error)
                    addLog("Erro ao processar as novels.")
                else if (!desativar)
                    addLog("Novels processadas com sucesso.")

                progress.barraProgresso.progressProperty().unbind()
                controller.barraProgresso.progressProperty().unbind()

                if (controller is NovelsImportarController)
                    controller.barraProgressoTextos.progressProperty().unbind()

                progress.log.textProperty().unbind()
                controller.habilitar()
                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de processamento da novel: " + super.getMessage())
                addLog("Erro na thread de processamento da novel: " + super.getMessage())
            }
        }

        progress.barraProgresso.progressProperty().bind(processarArquivos.progressProperty())
        if (controller is NovelsImportarController)
            (controller).barraProgressoTextos.progressProperty().bind(propTexto)

        controller.barraProgresso.progressProperty().bind(processarArquivos.progressProperty())
        progress.log.textProperty().bind(processarArquivos.messageProperty())
        val t = Thread(processarArquivos)
        t.start()
    }

    fun processarTabelas(tabelas: List<NovelTabela>) {
        error = false
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Novels - Processar tabelas"
        val processar: Task<Void> = object : Task<Void>() {

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

                            Progress = 0
                            Size = 0
                            mensagem = ""
                            tabelas.stream().filter { t -> t.isProcessar }.collect(Collectors.toList())
                                .forEach { tabela ->
                                    tabela.volumes.stream().filter { v -> v.isProcessar }
                                        .collect(Collectors.toList())
                                        .forEach { Size++ }
                                }

                            desativar = false
                            for (novel in tabelas) {
                                if (!novel.isProcessar) continue
                                V = 0
                                for (volume in novel.volumes) {
                                    if (!volume.isProcessar)
                                        continue

                                    V++
                                    updateMessage("Processando Vocabulários... " + volume.novel)
                                    updateProgress(++Progress, Size)
                                    mensagem = "Processando " + V + " de " + novel.volumes.size + " volumes." + " Novel: " + volume.novel
                                    updateMessage(mensagem)
                                    val callback: Callback<Array<Int>, Boolean> = Callback<Array<Int>, Boolean> { param ->
                                        Platform.runLater { updateMessage(mensagem + " - Processando itens...." + param[0] + '/' + param[1]) }
                                        true
                                    }

                                    when (volume.lingua) {
                                        Language.JAPANESE -> processarJapones(volume, callback)
                                        Language.ENGLISH -> processarIngles(volume, callback)
                                        else -> {}
                                    }

                                    if (desativar) break
                                    updateMessage("Salvando vocabulário...")
                                    updateProgress(++Progress, Size)
                                    serviceNovel.updateVocabularioVolume(novel.base, volume)

                                    if (desativar) {
                                        updateMessage("Revertendo a ultima alteração da Novel: " + volume.novel + " - Volume: " + volume.volume.toString())
                                        serviceNovel.updateCancel(novel.base, volume)
                                        break
                                    }

                                    Platform.runLater {
                                        if (TaskbarProgressbar.isSupported())
                                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.NORMAL)
                                    }
                                }
                                if (desativar)
                                    break
                            }
                            Platform.runLater {
                                if (TaskbarProgressbar.isSupported())
                                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                            }
                        }
                    } catch (e: IOException) {
                        LOGGER.error(e.message, e)
                        error = true
                        addLog("Erro ao processar as novels.")
                        addLog(e.message!!)
                        Platform.runLater {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR)
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    error = true
                    addLog("Erro ao processar as novels.")
                    addLog(e.message!!)
                    Platform.runLater {
                        if (TaskbarProgressbar.isSupported())
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, TaskbarProgressbar.Type.ERROR)
                    }
                }
                return null
            }

            @Override
            override fun succeeded() {
                super.failed()
                if (error)
                    AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", "Erro ao processar as novels.")
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.stackPane, controller.root, mutableListOf(), "Aviso", "Novels processadas com sucesso.")

                if (error)
                    addLog("Erro ao processar as novels.")
                else if (!desativar)
                    addLog("Novels processadas com sucesso.")

                progress.barraProgresso.progressProperty().unbind()
                controller.barraProgresso.progressProperty().unbind()
                progress.log.textProperty().unbind()
                controller.habilitar()
                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
            }

            @Override
            override fun failed() {
                super.failed()
                LOGGER.warn("Erro na thread de processamento da novel: " + super.getMessage())
                addLog("Erro na thread de processamento da novel: " + super.getMessage())
            }
        }

        progress.barraProgresso.progressProperty().bind(processar.progressProperty())
        controller.barraProgresso.progressProperty().bind(processar.progressProperty())
        progress.log.textProperty().bind(processar.messageProperty())
        val t = Thread(processar)
        t.start()
    }

    @Throws(SQLException::class)
    private fun processarJapones(volume: NovelVolume, callback: Callback<Array<Int>, Boolean>) {
        try {
            DictionaryFactory().create(
                "",
                SudachiTokenizer.readAll(FileInputStream(SudachiTokenizer.getPathSettings(MenuPrincipalController.controller.dicionario)))
            ).use { dict ->
                tokenizer = dict.create()
                mode = SudachiTokenizer.getModo(MenuPrincipalController.controller.modo)
                siteDicionario = MenuPrincipalController.controller.site

                validaHistorico = mutableSetOf()
                desativar = false
                val size: Array<Int> = arrayOf(0, 0)
                vocabVolume = mutableSetOf()
                size[0] = 0
                size[1] = 0

                volume.capitulos.forEach { c -> size[1] += c.textos.size }
                for (capitulo in volume.capitulos) {
                    vocabCapitulo = mutableSetOf()
                    vocabValida = mutableSetOf()
                    for (texto in capitulo.textos) {
                        size[0]++
                        callback.call(size)
                        gerarVocabulario(texto.texto)

                        if (desativar)
                            break
                    }
                    capitulo.vocabularios = vocabCapitulo
                    if (desativar)
                        break
                }

                volume.vocabularios = vocabVolume
                volume.processado = true
            }
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            error = false
        }
    }

    private fun getSignificado(kanji: String): String {
        if (kanji.trim().isEmpty()) return ""
        addLog("$kanji : Obtendo significado.")
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
        if (resultado.isEmpty())
            resultado = processaPalavras(palavra, desmembra.processarDesmembrar(palavra, MenuPrincipalController.controller.dicionario, Modo.A), Modo.A)
        return resultado
    }

    private fun processaPalavras(original: String, palavras: List<String>, modo: Modo): String {
        var desmembrado = ""
        try {
            for (palavra in palavras) {
                if (original.equals(palavra, true))
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
    private val japanese = "[\u3041-\u9FAF]".toRegex()

    @Throws(SQLException::class)
    private fun gerarVocabulario(frase: String) {
        var texto = frase
        val furigana: MutableMap<String, String> = mutableMapOf()
        if (texto.lowercase(Locale.getDefault()).contains("[ruby]")) {
            val matcher: Matcher = Pattern.compile("(\\[ruby\\][^\\ruby]*\\[\\\\ruby\\])").matcher(texto)
            if (matcher.find()) {
                for (i in 0 until matcher.groupCount()) {
                    val palavra = matcher.group(i).replace("\\[\\\\?ruby\\]".toRegex(), "")
                    val kanji = palavra.replace("\\[rt\\][ぁ-龯]*\\[\\\\rt\\]".toRegex(), "")
                    val furi = palavra.replace("[ぁ-龯]*\\[rt\\]".toRegex(), "").replace("\\[\\\\rt\\]".toRegex(), "")
                    if (!furigana.containsKey(kanji))
                        furigana[kanji] = furi
                }
            }
            texto = texto.replace("\\[rt\\][ぁ-龯]*\\[\\\\rt\\]".toRegex(), "").replace("\\[\\\\?ruby\\]".toRegex(), "")
        }
        for (m in tokenizer.tokenize(mode, texto)) {
            if (m.surface().matches(pattern)) {
                if (validaHistorico.contains(m.dictionaryForm())) {
                    val vocabulario: VocabularioExterno? = vocabHistorico.stream()
                        .filter { vocab -> m.dictionaryForm().equals(vocab.palavra, true) }
                        .findFirst().orElse(null)

                    if (vocabulario != null) {
                        vocabCapitulo.add(vocabulario)
                        vocabVolume.add(vocabulario)
                        continue
                    }
                }

                if (vocabErros.containsKey(m.dictionaryForm()) && vocabErros[m.dictionaryForm()]!! > 3)
                    continue

                if (!vocabValida.contains(m.dictionaryForm())) {
                    val palavra = vocabularioJaponesService.select(m.surface(), m.dictionaryForm()).orElse(null)
                    val kanji: String = texto.substring(m.begin(), m.end())
                    var leitura = ""

                    if (furigana.containsKey(kanji))
                        leitura = furigana[kanji] ?: ""

                    if (palavra != null) {
                        val vocabulario = VocabularioExterno(palavra.getId(), palavra.vocabulario, palavra.portugues, palavra.ingles, palavra.leitura, true)

                        // Usado apenas para correção em formas em branco.
                        if (palavra.formaBasica.isEmpty()) {
                            palavra.formaBasica = m.dictionaryForm()
                            palavra.leitura = m.readingForm()
                            vocabularioJaponesService.update(palavra)
                        }
                        if (leitura.isNotEmpty() && (palavra.leituraNovel == null || !palavra.leituraNovel.equals(leitura, true))) {
                            palavra.leituraNovel = leitura
                            vocabularioJaponesService.update(palavra)
                        }
                        validaHistorico.add(m.dictionaryForm())
                        vocabHistorico.add(vocabulario)
                        vocabValida.add(m.dictionaryForm())
                        vocabCapitulo.add(vocabulario)
                        vocabVolume.add(vocabulario)
                    } else {
                        var revisar: Revisar? = serviceJaponesRevisar.select(m.surface(), m.dictionaryForm()).orElse(null)
                        if (revisar == null) {
                            revisar = Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), leitura, revisado = false, isAnime = false, isManga = false, isNovel = true)
                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = m.surface() + " : Vocabulário novo." }
                            serviceJaponesRevisar.insert(revisar)
                            revisar.ingles = getSignificado(revisar.vocabulario)

                            if (revisar.ingles.isEmpty() && !revisar.formaBasica.equals(revisar.vocabulario, false))
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
                                    if (vocabErros.containsKey(m.dictionaryForm()))
                                        vocabErros.put(m.dictionaryForm(), vocabErros[m.dictionaryForm()]!! + 1)
                                    else
                                        vocabErros[m.dictionaryForm()] = 1
                                }
                            } else {
                                if (vocabErros.containsKey(m.dictionaryForm()))
                                    vocabErros[m.dictionaryForm()] = vocabErros.get(m.dictionaryForm())!! + 1
                                else
                                    vocabErros[m.dictionaryForm()] = 1
                            }
                            serviceJaponesRevisar.update(revisar)
                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "" }
                        } else {
                            if (!revisar.isNovel) {
                                revisar.isNovel = true
                                serviceJaponesRevisar.setIsNovel(revisar)
                            }
                            if (leitura.isNotEmpty() && (revisar.leituraNovel == null || !revisar.leituraNovel.equals(leitura, true))) {
                                revisar.leituraNovel = leitura
                                serviceJaponesRevisar.update(revisar)
                            }
                            serviceJaponesRevisar.incrementaVezesAparece(revisar.vocabulario)
                        }
                        val vocabulario = VocabularioExterno(revisar.getId(), revisar.vocabulario, revisar.portugues, revisar.ingles, m.readingForm(), false)
                        validaHistorico.add(m.dictionaryForm())
                        vocabHistorico.add(vocabulario)
                        vocabValida.add(m.dictionaryForm())
                        vocabCapitulo.add(vocabulario)
                        vocabVolume.add(vocabulario)
                    }
                }
            }
        }
    }

    private var palavraValida: MutableSet<String> = mutableSetOf()

    @Throws(SQLException::class)
    fun processarIngles(volume: NovelVolume, callback: Callback<Array<Int>, Boolean>) {
        validaHistorico = mutableSetOf()
        vocabVolume = mutableSetOf()
        palavraValida = mutableSetOf()

        val ignore: Pattern = Pattern.compile("[\\d]|[^a-zA-Z0-9_'çãáàéèíìúù]")
        val size: Array<Int> = arrayOf(0, 0)
        size[0] = 0
        size[1] = 0

        volume.capitulos.forEach { c -> size[1] += c.textos.size }
        for (capitulo in volume.capitulos) {
            vocabCapitulo = mutableSetOf()
            vocabValida = mutableSetOf()
            for (texto in capitulo.textos) {
                size[0]++
                callback.call(size)
                if (texto.texto.isNotEmpty()) {
                    val palavras: Set<String> = texto.texto.lowercase(Locale.getDefault()).split(" ")
                        .filter { txt -> !txt.trim().contains(" ") && txt.isNotEmpty() }
                        .distinct().toSet()
                    for (palavra in palavras) {
                        if (ignore.matcher(palavra).find())
                            continue

                        if (validaHistorico.contains(palavra)) {
                            val vocabulario = vocabHistorico.stream().filter { vocab -> palavra.equals(vocab.palavra, true) }.findFirst().orElse(null)
                            if (vocabulario != null) {
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
                                vocabCapitulo.add(vocabulario)
                                vocabVolume.add(vocabulario)
                            } else {
                                if (!palavraValida.contains(palavra.lowercase(Locale.getDefault()))) {
                                    val valido = serviceInglesRevisar.isValido(palavra)
                                    if (valido.isNotEmpty())
                                        palavraValida.add(valido)
                                }

                                var revisar = serviceInglesRevisar.select(palavra).orElse(null)
                                if (revisar == null) {
                                    revisar = Revisar(palavra, revisado = false, isAnime = false, isManga = false, isNovel = true)
                                    Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$palavra : Vocabulário novo." }
                                    addLog("$palavra : Vocabulário novo.")

                                    if (revisar.vocabulario.isNotEmpty()) {
                                        try {
                                            traducoes++
                                            if (traducoes > 3000) {
                                                traducoes = 0
                                                MenuPrincipalController.controller.contaGoogle = Utils.next(MenuPrincipalController.controller.contaGoogle)
                                            }
                                            Platform.runLater { MenuPrincipalController.controller.getLblLog().text = "$palavra : Obtendo tradução." }
                                            addLog("$palavra : Obtendo tradução.")
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
                                    if (!revisar.isNovel) {
                                        revisar.isNovel = true
                                        serviceInglesRevisar.setIsNovel(revisar)
                                    }
                                    serviceInglesRevisar.incrementaVezesAparece(revisar.vocabulario)
                                }

                                val vocabulario = VocabularioExterno(revisar.getId(), palavra, revisar.portugues, false)

                                validaHistorico.add(palavra)
                                vocabHistorico.add(vocabulario)
                                vocabValida.add(palavra)
                                vocabCapitulo.add(vocabulario)
                                vocabVolume.add(vocabulario)
                            }
                        }
                    }
                }
                if (desativar)
                    break
            }
            capitulo.vocabularios = vocabCapitulo
            if (desativar)
                break
        }
        volume.vocabularios = vocabVolume
        volume.processado = true
    }

    fun corrige() {
        // Função de correção ou movimentação de novels para outra tabela. Basta adicionar a condição abaixo na função de select
        //condicao += " AND Table_Name LIKE 'temp_volumes' ";
        try {
            try {
                DictionaryFactory().create(
                    "",
                    SudachiTokenizer.readAll(FileInputStream(SudachiTokenizer.getPathSettings(Dicionario.FULL)))
                ).use { dict ->
                    tokenizer = dict.create()
                    mode = SplitMode.A
                    LOGGER.info("Consultando a correção...")
                    val lista: List<NovelTabela> = serviceNovel.selectTabelas(todos = true, isLike = false, base = "", linguagem = Language.JAPANESE, novel = "")
                    LOGGER.info("Iniciando a correção...")
                    for (novel in lista) {
                        val oldBase: String = novel.base
                        LOGGER.info("Corrigindo a base $oldBase")
                        for (volume in novel.volumes) {
                            val newBase: String = getBase(Language.JAPANESE, volume.titulo)
                            if (oldBase.equals(newBase, true))
                                continue

                            LOGGER.info("Corrigindo a novel " + volume.titulo)
                            serviceNovel.salvarVolume(newBase, volume)
                            serviceNovel.delete(oldBase, volume)
                            LOGGER.info("Concluido a correção da novel " + volume.titulo)
                        }
                    }
                    LOGGER.info("Concluido a correção das novels.")
                }
            } catch (e: IOException) {
                LOGGER.error(e.message, e)
                error = false
            }
        } catch (ex: Exception) {
            LOGGER.error("Erro ao corrigir as listas", ex)
        }
    }
}