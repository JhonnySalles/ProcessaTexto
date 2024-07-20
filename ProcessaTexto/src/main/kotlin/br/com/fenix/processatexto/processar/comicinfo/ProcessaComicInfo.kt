package br.com.fenix.processatexto.processar.comicinfo

import br.com.fenix.processatexto.controller.mangas.MangasComicInfoController
import br.com.fenix.processatexto.fileparse.Parse
import br.com.fenix.processatexto.fileparse.ParseFactory
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.entities.comicinfo.MAL
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.comicinfo.ComicPageType
import br.com.fenix.processatexto.model.enums.comicinfo.Manga
import br.com.fenix.processatexto.service.ComicInfoServices
import br.com.fenix.processatexto.util.Utils
import br.com.fenix.processatexto.util.configuration.Configuracao
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.katsute.mal4j.MyAnimeList
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import jakarta.xml.bind.Unmarshaller
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.util.Callback
import javafx.util.Pair
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


object ProcessaComicInfo {

    private val LOGGER = LoggerFactory.getLogger(ProcessaComicInfo::class.java)

    private var WINRAR: String? = null
    private val PATTERN = ".*\\.(zip|cbz|rar|cbr|tar)$".toRegex()
    private const val COMICINFO = "ComicInfo.xml"
    private const val IMAGE_WIDTH = 170.0
    private const val IMAGE_HEIGHT = 300.0
    private var JAXBC: JAXBContext? = null
    private var CANCELAR_PROCESSAMENTO = false
    private var CANCELAR_VALIDACAO = false
    private lateinit var CONTROLLER: MangasComicInfoController
    private var MARCACAPITULO: String = ""
    private var IGNORAR_VINCULO_SALVO: Boolean = false
    private const val CONSULTA_MAL = true
    private const val CONSULTA_JIKAN = true
    private var SERVICE: ComicInfoServices? = null

    fun setPai(controller: MangasComicInfoController) {
        CONTROLLER = controller
    }

    fun cancelar() {
        CANCELAR_PROCESSAMENTO = true
        CANCELAR_VALIDACAO = true
    }

    fun validar(winrar: String, linguagem: Language, path: String, callback: Callback<Array<Long>, Boolean>) {
        WINRAR = winrar
        CANCELAR_VALIDACAO = false
        val arquivos = File(path)
        val size: Array<Long> = arrayOf(0,0)
        try {
            JAXBC = JAXBContext.newInstance(ComicInfo::class.java)
            SERVICE = ComicInfoServices()
            if (arquivos.isDirectory) {
                size[0] = 0
                size[1] = arquivos.listFiles().size.toLong()
                callback.call(size)
                for (arquivo in arquivos.listFiles()) {
                    val nome: String = arquivo.name
                    print("Validando o manga $nome")
                    val valido = valida(linguagem, arquivo)
                    if (valido.isEmpty()) {
                        LOGGER.info(" - OK. ")
                        gravalog(path, "Validando o manga $nome - OK.\n")
                    } else {
                        LOGGER.info(" - Arquivo possui pendências: ")
                        LOGGER.info(valido)
                        gravalog(path, "Validando o manga $nome - Arquivo possui pendências: \n")
                        gravalog(path, valido)
                    }
                    LOGGER.info("-".repeat(100))
                    gravalog(path, "-".repeat(100) + "\n")
                    size[0]++
                    callback.call(size)
                    if (CANCELAR_VALIDACAO) break
                }
            } else if (arquivos.isFile) {
                size[0] = 0
                size[1] = 1
                callback.call(size)
                val nome: String = arquivos.name
                print("Validando o manga $nome")
                val valido = valida(linguagem, arquivos)
                if (valido.isEmpty()) {
                    LOGGER.info(" - OK. ")
                    gravalog(path, "Validando o manga $nome - OK.\n")
                } else {
                    LOGGER.info(" - Arquivo possui pendências: ")
                    LOGGER.info(valido)
                    gravalog(path, "Validando o manga $nome - Arquivo possui pendências: \n")
                    gravalog(path, valido)
                }
                LOGGER.info("-".repeat(100))
                gravalog(path, "-".repeat(100) + "\n")
                size[0]++
                callback.call(size)
            }
        } catch (e: JAXBException) {
            LOGGER.error(e.message, e)
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        } finally {
            if (JAXBC != null)
                JAXBC = null
            if (SERVICE != null)
                SERVICE = null
        }
    }

    fun processa(winrar: String, linguagem: Language, path: String, marcaCapitulo: String, ignorarVinculoSalvo: Boolean, callback: Callback<Array<Long>, Boolean>) {
        WINRAR = winrar
        CANCELAR_PROCESSAMENTO = false
        MARCACAPITULO = marcaCapitulo
        IGNORAR_VINCULO_SALVO = ignorarVinculoSalvo
        MAL = MyAnimeList.withClientID(Configuracao.myAnimeListClient)
        val arquivos = File(path)
        val size: Array<Long> = arrayOf(0,0)
        try {
            JAXBC = JAXBContext.newInstance(ComicInfo::class.java)
            SERVICE = ComicInfoServices()
            if (arquivos.isDirectory) {
                size[0] = 0
                size[1] = arquivos.listFiles().size.toLong()
                callback.call(size)
                for (arquivo in arquivos.listFiles()) {
                    processa(linguagem, arquivo, null)
                    size[0]++
                    callback.call(size)
                    if (CANCELAR_PROCESSAMENTO) break
                }
            } else if (arquivos.isFile) {
                size[0] = 0
                size[1] = 1
                callback.call(size)
                processa(linguagem, arquivos, null)
                size[0]++
                callback.call(size)
            }
        } catch (e: JAXBException) {
            LOGGER.error(e.message, e)
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        } finally {
            if (JAXBC != null)
                JAXBC = null
            if (SERVICE != null)
                SERVICE = null
        }
    }

    fun processa(winrar: String, linguagem: Language, arquivo: String, idMal: Long): Boolean {
        WINRAR = winrar
        CANCELAR_PROCESSAMENTO = false
        MAL = MyAnimeList.withClientID(Configuracao.myAnimeListClient)
        val arquivos = File(arquivo)
        if (!arquivos.exists()) return false
        try {
            if (JAXBC == null)
                JAXBC = JAXBContext.newInstance(ComicInfo::class.java)
            SERVICE = ComicInfoServices()
            processa(linguagem, arquivos, idMal)
        } catch (e: JAXBException) {
            LOGGER.error(e.message, e)
            return false
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            return false
        } finally {
            if (JAXBC != null) JAXBC = null
            if (SERVICE != null) SERVICE = null
        }
        return true
    }

    private var MAL: MyAnimeList? = null
    private fun getNome(nome: String): String {
        var arquivo: String = Utils.getNome(nome)
        if (arquivo.lowercase(Locale.getDefault()).contains("volume"))
            arquivo = arquivo.substring(0, arquivo.lowercase(Locale.getDefault()).indexOf("volume"))
        else if (arquivo.lowercase(Locale.getDefault()).contains("capitulo"))
            arquivo = arquivo.substring(0, arquivo.lowercase(Locale.getDefault()).indexOf("capitulo"))
        else if (arquivo.lowercase(Locale.getDefault()).contains("capítulo"))
            arquivo = arquivo.substring(0, arquivo.lowercase(Locale.getDefault()).indexOf("capítulo"))
        else if (arquivo.contains("-")) arquivo =
            arquivo.substring(0, arquivo.lastIndexOf("-"))
        if (arquivo.endsWith(" - "))
            arquivo = arquivo.substring(0, arquivo.lastIndexOf(" - "))
        return arquivo
    }

    fun getById(idMal: Long, registro: MAL.Registro): Boolean {
        if (MAL == null)
            MAL = MyAnimeList.withClientID(Configuracao.myAnimeListClient)

        val manga: dev.katsute.mal4j.manga.Manga? = MAL!!.getManga(idMal)
        return if (manga != null) {
            registro.nome = manga.title
            registro.id = manga.id
            registro.imagem = ImageView(manga.mainPicture.mediumURL)
            if (registro.imagem != null) {
                registro.imagem!!.fitWidth = IMAGE_WIDTH
                registro.imagem!!.fitHeight = IMAGE_HEIGHT
            }
            true
        } else
            false
    }

    private const val API_JIKAN_CHARACTER = "https://api.jikan.moe/v4/manga/%s/characters"
    private val TITLE_PATERN = "[^\\w\\s]".toRegex()
    private val MANGA_PATERN = " - Volume[\\w\\W]*".toRegex()
    private const val DESCRIPTION_MAL = "Tagged with MyAnimeList on "
    private var MANGA: dev.katsute.mal4j.manga.Manga? = null
    private var MANGA_CHARACTER: Pair<Long, String>? = null
    private fun getIdMal(notas: String?): Long? {
        var id: Long? = null
        if (notas != null) {
            if (notas.contains(";")) {
                for (note in notas.split(";"))
                    if (note.lowercase(Locale.getDefault()).contains(DESCRIPTION_MAL.lowercase(Locale.getDefault())))
                        id = note.substring(note.indexOf("[Issue ID")).replace("[Issue ID", "").replace("]", "").trim().toLong()
            } else if (notas.lowercase(Locale.getDefault()).contains(DESCRIPTION_MAL.lowercase(Locale.getDefault())))
                id = notas.substring(notas.indexOf("[Issue ID")).replace("[Issue ID", "").replace("]", "").trim().toLong()
        }
        return id
    }

    private fun processaMal(arquivo: String, nome: String, info: ComicInfo, linguagem: Language, idMal: Long?) {
        try {
            var id = idMal
            var saved: Optional<ComicInfo> = Optional.empty()

            if (IGNORAR_VINCULO_SALVO)
                saved = SERVICE!!.select(info.comic!!, info.languageISO!!)

            if (id == null)
                id = getIdMal(info.notes)

            if (id == null) {
                if (saved.isPresent && saved.get().idMal!! > 0) {
                    id = saved.get().idMal
                    info.setId(saved.get().getId())
                }
            }
            var title: String = nome.replace(MANGA_PATERN, "").trim()
            if (MANGA == null || !title.equals(MANGA!!.title.replace(MANGA_PATERN, "").trim(), ignoreCase = true)) {
                MANGA = null
                if (id != null)
                    MANGA = MAL!!.getManga(id)
                else {
                    var search: List<dev.katsute.mal4j.manga.Manga>
                    val max = 2
                    var page = 0
                    do {
                        LOGGER.info("Realizando a consulta $page")
                        search = MAL!!.manga.withQuery(nome).withLimit(50).withOffset(page).search()
                        if (search != null && search.isNotEmpty())
                            for (item in search) {
                                LOGGER.info(item.title)
                                if (item.type === dev.katsute.mal4j.manga.property.MangaType.Manga &&
                                    title.equals(item.title.replace(TITLE_PATERN, "").trim(), true)) {
                                    LOGGER.info("Encontrado o manga " + item.title)
                                    MANGA = item
                                    break
                                }
                            }
                        if (page == 0 && MANGA == null) {
                            if (search != null && search.isNotEmpty()) {
                                val mal = MAL(arquivo, nome)
                                for (item in search) {
                                    val registro = mal.addRegistro(item.title, item.id, false)
                                    if (item.mainPicture.mediumURL != null)
                                        registro.imagem = ImageView(item.mainPicture.mediumURL)
                                    else if (item.pictures.isNotEmpty() && item.pictures[0].mediumURL != null)
                                        registro.imagem = ImageView(item.pictures.get(0).mediumURL)

                                    if (registro.imagem != null) {
                                        registro.imagem!!.fitWidth = IMAGE_WIDTH
                                        registro.imagem!!.fitHeight = IMAGE_HEIGHT
                                        registro.imagem!!.isPreserveRatio = true
                                    }
                                }
                                try {
                                    val parse: Parse = ParseFactory.create(arquivo)
                                    mal.imagem = ImageView(Image(parse.getPagina(0)))
                                    mal.imagem!!.fitWidth = IMAGE_WIDTH
                                    mal.imagem!!.fitHeight = IMAGE_HEIGHT
                                    mal.imagem!!.isPreserveRatio = true
                                } catch (e: Exception) {
                                    LOGGER.error(e.message, e)
                                }
                                mal.myanimelist[0].isMarcado = true
                                Platform.runLater { CONTROLLER.addItem(mal) }
                            }
                        }
                        page++
                        if (page > max)
                            break
                    } while (MANGA == null && search != null && search.isNotEmpty())
                }
            }
            if (MANGA != null) {
                if (info.getId() == null) {
                    if (saved.isPresent) 
                        info.setId(saved.get().getId())
                    info.idMal = id
                    SERVICE!!.save(info)
                }
                for (author in MANGA!!.authors) {
                    if (author.role.equals("art", ignoreCase = true)) {
                        if (info.penciller == null || info.penciller!!.isEmpty())
                            info.penciller = (author.firstName + " " + author.lastName).trim()
                        
                        if (info.inker == null || info.inker!!.isEmpty())
                            info.inker = (author.firstName + " " + author.lastName).trim()
                        
                        if (info.coverArtist == null || info.coverArtist!!.isEmpty())
                            info.coverArtist = (author.firstName + " " + author.lastName).trim()
                    } else if (author.role.equals("story", ignoreCase = true)) {
                        if (info.penciller == null || info.penciller!!.isEmpty())
                            info.penciller = (author.firstName + " " + author.lastName).trim()
                    } else {
                        if (author.role.lowercase(Locale.getDefault()).contains("story")) {
                            if (info.writer == null || info.penciller!!.isEmpty())
                                info.writer = (author.firstName + " " + author.lastName).trim()
                        }
                        if (author.role.lowercase(Locale.getDefault()).contains("art")) {
                            if (info.penciller == null || info.penciller!!.isEmpty()) 
                                info.penciller = (author.firstName + " " + author.lastName).trim()
                            
                            if (info.inker == null || info.inker!!.isEmpty()) 
                                info.inker = (author.firstName + " " + author.lastName).trim()
                            
                            if (info.coverArtist == null || info.coverArtist!!.isEmpty()) 
                                info.coverArtist = (author.firstName + " " + author.lastName).trim()
                        }
                    }
                }
                if (info.genre == null || info.genre!!.isEmpty()) {
                    var genero = ""
                    for (genre in MANGA!!.genres)
                        genero += genre.name + "; "
                    info.genre = genero.substring(0, genero.lastIndexOf("; "))
                }
                if (linguagem == Language.PORTUGUESE) {
                    if (MANGA!!.alternativeTitles.english != null && MANGA!!.alternativeTitles.english.isNotEmpty()) {
                        info.title = MANGA!!.title
                        info.series = MANGA!!.alternativeTitles.english
                    }
                } else if (linguagem == Language.JAPANESE) {
                    if (MANGA!!.alternativeTitles.japanese != null && MANGA!!.alternativeTitles.japanese.isNotEmpty())
                        info.title = MANGA!!.alternativeTitles.japanese
                }
                if (info.alternateSeries == null || info.alternateSeries!!.isEmpty()) {
                    title = ""
                    if (MANGA!!.alternativeTitles.japanese != null && MANGA!!.alternativeTitles.japanese.isNotEmpty())
                        title += MANGA!!.alternativeTitles.japanese + "; "

                    if (MANGA!!.alternativeTitles.english != null && MANGA!!.alternativeTitles.english.isNotEmpty())
                        title += MANGA!!.alternativeTitles.english + "; "


                    if (MANGA!!.alternativeTitles.synonyms != null)
                        for (synonym in MANGA!!.alternativeTitles.synonyms)
                            title += "$synonym; "

                    if (title.isNotEmpty())
                        info.alternateSeries = title.substring(0, title.lastIndexOf("; "))
                }

                if (info.publisher == null || info.publisher!!.isEmpty()) {
                    var publisher = ""
                    for (pub in MANGA!!.serialization)
                        publisher += pub.name + "; "

                    if (publisher.isNotEmpty())
                        info.publisher = publisher.substring(0, publisher.lastIndexOf("; "))
                }

                val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                var notes = ""
                if (info.notes != null) {
                    if (info.notes!!.contains(";")) {
                        for (note in info.notes!!.split(";"))
                            notes += if (note.lowercase(Locale.getDefault()).contains(DESCRIPTION_MAL.lowercase(Locale.getDefault())))
                                DESCRIPTION_MAL + dateTime.format(LocalDateTime.now()) + ". [Issue ID " + MANGA!!.id + "]; "
                            else
                                note.trim() + "; "
                    } else
                        notes += ((info.notes + "; " + DESCRIPTION_MAL + dateTime.format(LocalDateTime.now())) + ". [Issue ID " + MANGA!!.id) + "]; "
                } else
                    notes += DESCRIPTION_MAL + dateTime.format(LocalDateTime.now()) + ". [Issue ID " + MANGA!!.id + "]; "

                info.notes = notes.substring(0, notes.lastIndexOf("; "))

                if (CONSULTA_JIKAN) {
                    if (MANGA_CHARACTER != null && MANGA_CHARACTER!!.value.equals(MANGA!!.id))
                        info.characters = MANGA_CHARACTER!!.value
                    else {
                        MANGA_CHARACTER = null
                        try {
                            val reqBuilder: HttpRequest.Builder = HttpRequest.newBuilder()
                            val request: HttpRequest = reqBuilder
                                .uri(URI(String.format(API_JIKAN_CHARACTER, MANGA!!.id)))
                                .GET()
                                .build()
                            val response: HttpResponse<String> = HttpClient.newBuilder()
                                .build()
                                .send(request, HttpResponse.BodyHandlers.ofString())

                            val responseBody: String = response.body()
                            if (responseBody.contains("character")) {
                                val gson = Gson()
                                val element: JsonElement = gson.fromJson(responseBody, JsonElement::class.java)
                                val jsonObject: JsonObject = element.asJsonObject
                                val list: JsonArray = jsonObject.getAsJsonArray("data")

                                var characters = ""
                                for (item in list) {
                                    val obj: JsonObject = item.asJsonObject
                                    var character: String = obj.getAsJsonObject("character").get("name").asString
                                    if (character.contains(", "))
                                        character = character.replace(",", "")
                                    else if (character.contains(","))
                                        character = character.replace(",", " ")

                                    characters += character + if (obj.get("role").asString.equals("main", true)) " (" + obj.get("role").asString + "), " else ", "
                                }
                                if (characters.isNotEmpty()) {
                                    info.characters = characters.substring(0, characters.lastIndexOf(", ")) + "."
                                    MANGA_CHARACTER = Pair(MANGA!!.id!!, info.characters!!)
                                }
                            }
                        } catch (e: Exception) {
                            LOGGER.error(e.message, e)
                            LOGGER.info(MANGA!!.id.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
    }

    private fun processa(linguagem: Language, arquivo: File, idMal: Long?) {
        if (arquivo.name.lowercase(Locale.getDefault()).matches(PATTERN)) {
            var info: File? = null
            try {
                info = extraiInfo(arquivo, false)

                if (info == null || !info.exists())
                    return

                val comic: ComicInfo = try {
                    val unmarshaller = JAXBC!!.createUnmarshaller()
                    unmarshaller.unmarshal(info) as ComicInfo
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    return
                }
                val nome = getNome(arquivo.name)
                LOGGER.info("Processando o manga $nome")
                if (nome.contains("-"))
                    comic.comic = nome.substring(0, nome.lastIndexOf("-")).trim()
                else if (nome.contains("."))
                    comic.comic = nome.substring(0, nome.lastIndexOf(".")).trim()
                else
                    comic.comic = nome

                comic.manga = Manga.Yes
                comic.languageISO = linguagem.sigla

                if (comic.title == null || comic.title!!.lowercase(Locale.getDefault()).contains("vol.") || comic.title!!.lowercase(Locale.getDefault()).contains("volume"))
                    comic.title = comic.series
                else if (comic.title != null && !comic.title.equals(comic.series, true))
                    comic.storyArc = comic.title

                if (CONSULTA_MAL)
                    processaMal(arquivo.absolutePath, nome, comic, linguagem, idMal)
                val titulosCapitulo: MutableList<Pair<Float, String>> = mutableListOf()
                if (comic.summary != null && comic.summary!!.isNotEmpty()) {
                    val sumary = comic.summary!!.lowercase(Locale.getDefault())
                    if (sumary.contains("chapter titles") || sumary.contains("chapter list") || sumary.contains("contents")) {
                        val linhas: List<String> = comic.summary!!.split("\n")
                        for (linha in linhas) {
                            var number = 0f
                            var chapter = ""
                            if (linha.matches("([\\w. ]+[\\d][:|.][\\w\\W]++)|([\\d][:|.][\\w\\W]++)".toRegex())) {
                                var aux: List<String> = listOf()
                                if (linha.contains(":"))
                                    aux = linha.split(":")
                                else if (linha.contains(". "))
                                    aux = linha.replace(". ", ":").split(":")

                                if (aux.isNotEmpty()) {
                                    try {
                                        number = if (aux[0].matches("[a-zA-Z ]+[.][\\d]".toRegex())) // Ex: Act.1: Spring of the Dead
                                            aux[0].replace("[^\\d]".toRegex(), "").toFloat()
                                        else if (aux[0].lowercase(Locale.getDefault()).contains("extra") || aux[0].lowercase(Locale.getDefault()).contains("special"))
                                            -1f
                                        else
                                            aux[0].replace("[^\\d.]".toRegex(), "").toFloat()

                                        chapter = aux[1].trim()
                                    } catch (e: Exception) {
                                        LOGGER.error(e.message, e)
                                    }
                                }
                            }
                            if (number.compareTo(0f) > 0) titulosCapitulo.add(Pair(number, chapter))
                        }
                    }
                }
                val parse = ParseFactory.create(arquivo)
                try {
                    if (linguagem == Language.PORTUGUESE) {
                        var tradutor = ""
                        for (pasta in parse.getPastas().keys) {
                            var item = ""
                            if (pasta.contains("]")) item = pasta.substring(0, pasta.indexOf("]")).replace("[", "")
                            if (item.isNotEmpty()) {
                                if (item.contains("&")) {
                                    val itens: List<String> = item.split("&")
                                    for (itm in itens)
                                        if (!tradutor.contains(itm.trim()))
                                            tradutor += itm.trim() + "; "
                                } else if (!tradutor.contains(item))
                                    tradutor += "$item; "
                            }
                        }
                        if (tradutor.isNotEmpty()) {
                            comic.translator = tradutor.substring(0, tradutor.length - 2)
                            comic.scanInformation = tradutor.substring(0, tradutor.length - 2)
                        }
                    } else if (linguagem == Language.JAPANESE) {
                        comic.translator = ""
                        comic.scanInformation = ""
                    }

                    comic.pages?.forEach {
                        it.bookmark = null
                        it.type = null
                    }

                    val pastas: Map<String, Int> = parse.getPastas()
                    var index = 0
                    for (i in 0 until parse.getSize()) {
                        if (index >= comic.pages!!.size)
                            continue

                        if (Utils.isImage(parse.getPaginaPasta(i))) {
                            val imagem: String = parse.getPaginaPasta(i).lowercase(Locale.getDefault())
                            val page = comic.pages!!.get(index)
                            if (imagem.contains("frente")) {
                                page.bookmark = "Cover"
                                page.type = ComicPageType.FrontCover
                            } else if (imagem.contains("tras")) {
                                page.bookmark = "Back"
                                page.type = ComicPageType.BackCover
                            } else if (imagem.contains("tudo")) {
                                page.bookmark = "All cover"
                                page.doublePage = true
                                page.type = ComicPageType.Other
                            } else if (imagem.contains("zsumário") || imagem.contains("zsumario")) {
                                page.bookmark = "Sumary"
                                page.type = ComicPageType.InnerCover
                            } else {
                                if (pastas.containsValue(i)) {
                                    var capitulo = ""
                                    for (entry in pastas.entries) {
                                        if (entry.value == i) {
                                            if (entry.key.lowercase(Locale.getDefault()).contains("capitulo"))
                                                capitulo = entry.key.substring(entry.key.lowercase(Locale.getDefault()).indexOf("capitulo"))
                                            else if (entry.key.lowercase(Locale.getDefault()).contains("capítulo"))
                                                capitulo = entry.key.substring(entry.key.lowercase(Locale.getDefault()).indexOf("capítulo"))

                                            if (capitulo.isNotEmpty()) {
                                                if (MARCACAPITULO.isNotEmpty()) {
                                                    capitulo = if (capitulo.lowercase(Locale.getDefault()).contains("capítulo"))
                                                        capitulo.substring(capitulo.lowercase(Locale.getDefault()).indexOf("capítulo") + 8)
                                                    else
                                                        capitulo.substring(capitulo.lowercase(Locale.getDefault()).indexOf("capitulo") + 8)

                                                    capitulo = if (MARCACAPITULO.lowercase(Locale.getDefault()).contains("%s")) // Japanese
                                                        MARCACAPITULO.lowercase(Locale.getDefault()).replace("%s", capitulo.trim())
                                                    else
                                                        MARCACAPITULO + capitulo
                                                }
                                                break
                                            }
                                        }
                                    }
                                    if (capitulo.isNotEmpty()) {
                                        if (titulosCapitulo.isNotEmpty()) {
                                            try {
                                                val number = capitulo.replace("[^\\d.]".toRegex(), "").toFloat()
                                                val titulo: Optional<Pair<Float, String>> = titulosCapitulo.stream().filter { it.key == number }.findFirst()
                                                if (titulo.isPresent) {
                                                    capitulo += " - " + titulo.get().value
                                                    titulosCapitulo.remove(titulo.get())
                                                }
                                            } catch (e: Exception) {
                                                LOGGER.error(e.message, e)
                                            }
                                        }
                                        page.bookmark = capitulo
                                    }
                                }
                                if (page.imageWidth == null || page.imageHeight == null) {
                                    try {
                                        val image = Image(parse.getPagina(i))
                                        page.imageWidth = image.width.toInt()
                                        page.imageWidth = image.height.toInt()
                                    } catch (e: IOException) {
                                        LOGGER.error(e.message, e)
                                    }
                                }
                                if (page.imageWidth != null && page.imageHeight != null && page.imageHeight!! > 0)
                                    if (page.imageWidth!! / page.imageHeight!! > 0.9)
                                        page.doublePage = true
                            }
                            index++
                        }
                    }
                } finally {
                    Utils.destroiParse(parse)
                }

                try {
                    val marshaller = JAXBC!!.createMarshaller()
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                    val out = FileOutputStream(info)
                    marshaller.marshal(comic, out)
                    out.close()
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    return
                }
                insereInfo(arquivo, info)
            } finally {
                info?.delete()
            }
        }
    }

    private fun extraiInfo(arquivo: File, silent: Boolean): File {
        var comicInfo = File("")
        var proc: Process? = null
        val comando = "cmd.exe /C cd \"" + WINRAR + "\" &&rar e -y " + '"' + arquivo.path + '"' + " " + '"' + Utils.getCaminho(arquivo.path) + '"' + " " + '"' + COMICINFO + '"'
        if (!silent)
            LOGGER.info("rar e -y " + '"' + arquivo.path + '"' + " " + '"' + Utils.getCaminho(arquivo.path) + '"' + " " + '"' + COMICINFO + '"')
        try {
            val rt: Runtime = Runtime.getRuntime()
            proc = rt.exec(comando)
            if (!silent)
                LOGGER.info("Resultado: " + proc.waitFor())
            var resultado = ""
            val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
            var s: String?
            while (stdInput.readLine().also { s = it } != null)
                resultado += "$s"
            if (!silent && resultado.isNotEmpty())
                LOGGER.info("Output comand:\n$resultado")
            s = null
            var error = ""
            val stdError = BufferedReader(InputStreamReader(proc.errorStream))
            while (stdError.readLine().also { s = it } != null)
                error += "$s"
            if (resultado.isEmpty() && error.isNotEmpty())
                LOGGER.info("Error comand: $resultado Não foi possível extrair o arquivo $COMICINFO.")
            else
                comicInfo = File(Utils.getCaminho(arquivo.path) + '\\' + COMICINFO)
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        } finally {
            proc?.destroy()
        }
        return comicInfo
    }

    private fun insereInfo(arquivo: File, info: File) {
        val comando = ("cmd.exe /C cd \"" + WINRAR + "\" &&rar a -ep " + '"' + arquivo.path + '"' + " " + '"' + info.path + '"')
        LOGGER.info("rar a -ep " + '"' + arquivo.path + '"' + " " + '"' + info.path + '"')
        var proc: Process? = null
        try {
            val rt: Runtime = Runtime.getRuntime()
            proc = rt.exec(comando)
            LOGGER.info("Resultado: " + proc.waitFor())
            var resultado = ""
            val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
            var s: String? = null
            while (stdInput.readLine().also { s = it } != null)
                resultado += "$s"

            if (resultado.isNotEmpty())
                LOGGER.info("Output comand:\n$resultado")
            s = null
            var error = ""
            val stdError = BufferedReader(InputStreamReader(proc.errorStream))
            while (stdError.readLine().also { s = it } != null)
                error += "$s"

            if (resultado.isEmpty() && error.isNotEmpty()) {
                info.renameTo(File(arquivo.path + Utils.getNome(arquivo.name) + Utils.getExtenssao(info.name)))
                LOGGER.info("Error comand:\n$resultado\nNecessário adicionar o rar no path e reiniciar a aplicação.")
            } else
                info.delete()
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        } finally {
            proc?.destroy()
        }
    }

    private fun valida(linguagem: Language, arquivo: File): String {
        var valida = ""
        if (arquivo.name.lowercase(Locale.getDefault()).matches(PATTERN)) {
            var info: File? = null
            try {
                info = extraiInfo(arquivo, true)
                if (info == null || !info.exists())
                    return "Comic info não encontrado"

                val comic: ComicInfo = try {
                    val unmarshaller: Unmarshaller = JAXBC!!.createUnmarshaller()
                    unmarshaller.unmarshal(info) as ComicInfo
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                    return "Não foi possível realizar a extração do Comic info."
                }
                if (arquivo.name.contains("-"))
                    comic.comic = arquivo.name.substring(0, arquivo.name.lastIndexOf("-")).trim()
                else if (arquivo.name.contains("."))
                    comic.comic = arquivo.name.substring(0, arquivo.name.lastIndexOf(".")).trim()
                else
                    comic.comic = arquivo.name

                try {
                    val saved: Optional<ComicInfo> = SERVICE!!.select(comic.comic!!, comic.languageISO!!)
                    if (saved.isEmpty || comic.idMal == null) {
                        if (saved.isPresent)
                            comic.setId(saved.get().getId())
                        comic.idMal = getIdMal(comic.notes)
                        SERVICE!!.save(comic)
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }

                if (comic.manga == null)
                    valida += "Tipo de manga ausente. \n"
                if (comic.languageISO == null || comic.languageISO!!.isEmpty())
                    valida += "Linguagem ausente. \n"
                if (comic.title == null || comic.title!!.lowercase(Locale.getDefault()).contains("vol.") || comic.title!!.lowercase(Locale.getDefault()).contains("volume"))
                    valida += "Título ausente. \n"

                if (linguagem == Language.PORTUGUESE) {
                    if (comic.translator == null || comic.translator!!.isEmpty())
                        valida += "Tradutor ausente. \n"
                    if (comic.scanInformation == null || comic.scanInformation!!.isEmpty())
                        valida += "Informação da scan ausente. \n"
                }
                var bookmarks = false
                for (page in comic.pages!!) {
                    if (page.bookmark != null && page.bookmark!!.isNotEmpty()) {
                        bookmarks = true
                        break
                    }
                }
                if (!bookmarks)
                    valida += "Bookmars ausentes. \n"

                var images = true
                for (page in comic.pages!!) {
                    if (page.imageWidth == null || page.imageHeight == null) {
                        images = false
                        break
                    }
                }

                if (!images)
                    valida += "Tamanho de imagens ausentes. \n"

                if (comic.year == null || comic.year === 0)
                    valida += "Publicação: Ano ausente. \n"
                if (comic.month == null || comic.month === 0)
                    valida += "Publicação: Mês ausente. \n"
                if (comic.day == null || comic.day === 0)
                    valida += "Publicação: Dia ausente. \n"

                try {
                    LocalDate.of(comic.year!!, comic.month!!, comic.day!!)
                } catch (e: Exception) {
                    valida += if (comic.year != null && comic.month != null && comic.day != null)
                        "Publicação: Data inválida. (${comic.day}/${comic.month}/${comic.year})."
                    else
                        "Publicação: Data inválida. \n"
                }
                if (valida.isNotEmpty())
                    valida = valida.substring(0, valida.length - 2)
            } finally {
                info?.delete()
            }
        } else
            valida = "Nome inválido"

        return valida
    }

    private const val ARQUIVO_LOG = "Log.txt"
    private fun gravalog(diretorio: String, texto: String) {
        try {
            var arquivo = diretorio + '\\' + ARQUIVO_LOG
            if (arquivo.contains("\\" + "\\"))
                arquivo = arquivo.replace("\\" + "\\", "\\")

            val log = File(arquivo)
            if (!log.exists())
                log.createNewFile()

            val fw = FileWriter(arquivo, true)
            fw.write(texto)
            fw.close()
        } catch (ioe: IOException) {
            System.err.println("IOException: " + ioe.message)
        }
    }
}