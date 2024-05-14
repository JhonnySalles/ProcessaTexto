package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.components.listener.VinculoServiceListener
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.fileparse.Parse
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaPagina
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVinculo
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.entities.processatexto.Vinculo
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Pagina
import br.com.fenix.processatexto.util.Utils
import br.com.fenix.processatexto.util.similarity.ImageHistogram
import br.com.fenix.processatexto.util.similarity.ImagePHash
import javafx.collections.ObservableList
import javafx.concurrent.Task
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors


class VincularServices {

    private val LOGGER = LoggerFactory.getLogger(VincularServices::class.java)

    private val dao = DaoFactory.createVincularDao()
    private val mangaDao = DaoFactory.createMangaDao()

    @Throws(SQLException::class)
    fun salvar(base: String, obj: Vinculo) {
        if (base.isEmpty())
            return

        if (obj.getId() == null)
            insert(base, obj)
        else
            update(base, obj)
    }

    @Throws(SQLException::class)
    fun update(base: String?, obj: Vinculo?) {
        if (base == null || base.isEmpty()) return
        dao!!.update(base, obj!!)
    }

    fun selectVolume(base: String, manga: String, volume: Int, linguagem: Language): Optional<MangaVolume> {
        return if (base.isEmpty())
            Optional.empty()
        else
            try {
                mangaDao!!.selectVolume(base, manga, volume, linguagem)
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                Optional.empty()
            }
    }

    @Throws(SQLException::class)
    fun select(base: String, id: UUID): Optional<Vinculo> {
        return if (base.isEmpty()) Optional.empty() else dao!!.select(base, id)
    }

    @Throws(SQLException::class)
    fun select(
        base: String, volume: Int, mangaOriginal: String, linguagemOriginal: Language?,
        arquivoOriginal: String, mangaVinculado: String, linguagemVinculado: Language?, arquivoVinculado: String
    ): Optional<Vinculo> {
        if (base.isEmpty())
            return Optional.empty()

        return if (linguagemOriginal != null && linguagemVinculado != null && (mangaOriginal.isBlank()) && (mangaOriginal.isBlank()))
            select(base, volume, mangaOriginal, linguagemOriginal, mangaVinculado, linguagemVinculado)
        else if (linguagemOriginal == null && linguagemVinculado == null && mangaOriginal.isNotBlank() && mangaOriginal.isNotBlank())
            select(base, volume, mangaOriginal, arquivoOriginal, mangaVinculado, arquivoVinculado)
        else
            dao!!.select(base, volume, mangaOriginal, linguagemOriginal, arquivoOriginal, mangaVinculado, linguagemVinculado, arquivoVinculado)
    }

    @Throws(SQLException::class)
    fun select(base: String, volume: Int, mangaOriginal: String, original: String, mangaVinculado: String, vinculado: String): Optional<Vinculo> {
        return if (base.isEmpty()) Optional.empty() else dao!!.select(base, volume, mangaOriginal, original, mangaVinculado, vinculado)
    }

    @Throws(SQLException::class)
    fun select(base: String, volume: Int, mangaOriginal: String, linguagemOriginal: Language, mangaVinculado: String, linguagemVinculado: Language): Optional<Vinculo> {
        return if (base.isEmpty()) Optional.empty() else dao!!.select(base, volume, mangaOriginal, linguagemOriginal, mangaVinculado, linguagemVinculado)
    }

    @Throws(SQLException::class)
    fun delete(base: String, obj: Vinculo) {
        if (base == null || base.isEmpty())
            return

        dao!!.delete(base, obj)
    }

    @Throws(SQLException::class)
    fun insert(base: String, obj: Vinculo): UUID? {
        return if (base.isEmpty()) null else dao!!.insert(base, obj)
    }

    @Throws(SQLException::class)
    fun createTabelas(base: String): Boolean {
        return if (base.isEmpty()) false else dao!!.createTabelas(base)
    }

    @get:Throws(SQLException::class)
    val tabelas: List<String> get() = dao!!.tabelas

    @Throws(SQLException::class)
    fun getMangas(base: String, linguagem: Language?): List<String> {
        return if (base.isEmpty() || linguagem == null) listOf() else dao!!.getMangas(base, linguagem)
    }

    @Throws(SQLException::class)
    fun selectTabelasJson(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language): MutableList<MangaTabela> {
        return dao!!.selectTabelasJson(base, manga, volume, capitulo, linguagem)
    }

    @Throws(SQLException::class)
    fun getMangaVinculo(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language): MutableList<MangaVinculo> {
        return dao!!.selectVinculo(base, manga, volume, capitulo, linguagem)
    }

    // -------------------------------------------------------------------------------------------------
    fun gerarAtributos(parse: Parse, isManga: Boolean) {
        val progress = MenuPrincipalController.controller.criaBarraProgresso()
        progress!!.titulo.text = "Processando atributos da imagem do arquivo " + if (isManga) "original" else "vinculado"
        val processar: Task<Void> = object : Task<Void>() {
            var I: Long = 0
            var Max: Long = 0

            @Override
            @Throws(Exception::class)
            override fun call(): Void? {
                try {
                    val lista: MutableList<VinculoPagina> = listener.vinculados.parallelStream().collect(Collectors.toList())
                    if (!isManga)
                        lista.addAll(listener.naoVinculados)
                    I = 0
                    Max = lista.size.toLong() * 2
                    val imgPHash = ImagePHash()
                    val imgHistogram = ImageHistogram()
                    updateMessage("Gerando pHash....")
                    for (pagina in lista) {
                        if (isManga)
                            pagina.originalPHash = imgPHash.getHash(parse.getPagina(pagina.originalPagina))
                        else {
                            if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
                                pagina.vinculadoEsquerdaPHash = imgPHash.getHash(parse.getPagina(pagina.vinculadoEsquerdaPagina))
                            if (pagina.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA)
                                pagina.vinculadoDireitaPHash = imgPHash.getHash(parse.getPagina(pagina.vinculadoDireitaPagina))
                        }
                        I++
                        updateProgress(I, Max)
                    }
                    updateMessage("Gerando Histogram....")
                    for (pagina in lista) {
                        if (isManga)
                            pagina.originalHistogram = imgHistogram.generate(parse.getPagina(pagina.originalPagina))
                        else {
                            if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
                                pagina.vinculadoEsquerdaHistogram = imgHistogram.generate(parse.getPagina(pagina.vinculadoEsquerdaPagina))
                            if (pagina.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA)
                                pagina.vinculadoDireitaHistogram = imgHistogram.generate(parse.getPagina(pagina.vinculadoDireitaPagina))
                        }
                        I++
                        updateProgress(I, Max)
                    }
                } catch (e: Exception) {
                    LOGGER.error(e.message, e)
                }
                return null
            }

            @Override
            override fun succeeded() {
                progress.barraProgresso.progressProperty().unbind()
                progress.log.textProperty().unbind()
                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
            }
        }
        progress.log.textProperty().bind(processar.messageProperty())
        progress.barraProgresso.progressProperty().bind(processar.progressProperty())
        val t = Thread(processar)
        t.start()
    }

    // -------------------------------------------------------------------------------------------------
    private lateinit var listener: VinculoServiceListener
    fun setListener(listener: VinculoServiceListener) {
        this.listener = listener
    }

    fun addNaoVinculado(pagina: VinculoPagina) {
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
            naoVinculado.add(VinculoPagina(pagina, isEsquerda = true, isNaoVinculado = true))

        if (pagina.isImagemDupla) {
            naoVinculado.add(VinculoPagina(pagina, false, isNaoVinculado = true))
            pagina.limparVinculadoDireita()
        }
    }

    fun ordenarPaginaDupla(isUsePaginaDuplaCalculada: Boolean) {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty())
            return

        var padding: Int = 1
        for (pagina in vinculado) {
            val index: Int = vinculado.indexOf(pagina)
            if (pagina.isImagemDupla || isUsePaginaDuplaCalculada && pagina.isVinculadoEsquerdaPaginaDupla)
                continue

            if (index + padding >= vinculado.size)
                break
            var proximo: VinculoPagina = vinculado[index + padding]
            if (proximo.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                do {
                    padding++
                    if (index + padding >= vinculado.size)
                        break
                    proximo = vinculado[index + padding]
                } while (proximo.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA)
            }
            if (index + padding >= vinculado.size)
                break
            if (proximo.isImagemDupla || isUsePaginaDuplaCalculada && pagina.isVinculadoEsquerdaPaginaDupla) {
                if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) continue else pagina.mesclar(proximo)
                proximo.limparVinculado()
            } else {
                if (isUsePaginaDuplaCalculada) {
                    if (pagina.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                        pagina.addVinculoEsquerda(proximo)
                        proximo.limparVinculadoEsquerda()
                        if (!pagina.isVinculadoEsquerdaPaginaDupla) {
                            if (proximo.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                pagina.addVinculoDireita(proximo)
                                proximo.limparVinculadoDireita()
                            } else {
                                padding++
                                if (index + padding >= vinculado.size) {
                                    padding--
                                    continue
                                }
                                proximo = vinculado[index + padding]
                                if (proximo.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA && !proximo.isImagemDupla && !proximo.isVinculadoEsquerdaPaginaDupla) {
                                    pagina.addVinculoDireitaApartirEsquerda(proximo)
                                    proximo.limparVinculadoEsquerda()
                                } else padding--
                            }
                        }
                    } else {
                        if (proximo.isVinculadoEsquerdaPaginaDupla) continue else {
                            pagina.addVinculoDireitaApartirEsquerda(proximo)
                            proximo.limparVinculadoEsquerda()
                        }
                    }
                } else {
                    if (pagina.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                        pagina.addVinculoEsquerda(proximo)
                        proximo.limparVinculadoEsquerda()
                        if (proximo.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA) {
                            pagina.addVinculoDireita(proximo)
                            proximo.limparVinculadoDireita()
                        } else {
                            padding++
                            if (index + padding >= vinculado.size) {
                                padding--
                                continue
                            }
                            proximo = vinculado[index + padding]
                            if (!proximo.isImagemDupla
                                && proximo.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA
                            ) {
                                pagina.addVinculoEsquerdaApartirDireita(proximo)
                                proximo.limparVinculadoEsquerda()
                            } else padding--
                        }
                    } else {
                        pagina.addVinculoDireitaApartirEsquerda(proximo)
                        proximo.limparVinculadoEsquerda()
                    }
                }
            }
        }
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        if (!naoVinculado.isEmpty()) {
            val paginasNaoVinculado: MutableList<VinculoPagina> =
                naoVinculado.stream().sorted { a: VinculoPagina, _: VinculoPagina? -> a.vinculadoEsquerdaPagina.compareTo(a.vinculadoEsquerdaPagina) }
                    .collect(Collectors.toList())
            for (pagina in vinculado) {
                if (paginasNaoVinculado.isEmpty())
                    break

                if (pagina.isImagemDupla || isUsePaginaDuplaCalculada && pagina.isVinculadoEsquerdaPaginaDupla)
                    continue

                if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
                    pagina.addVinculoDireitaApartirEsquerda(paginasNaoVinculado.removeAt(0))
                else if (isUsePaginaDuplaCalculada) {
                    val notLinked: VinculoPagina = paginasNaoVinculado.removeAt(0)
                    pagina.addVinculoDireita(notLinked)
                    if (notLinked.isVinculadoEsquerdaPaginaDupla) continue
                    if (paginasNaoVinculado.isEmpty()) break
                    pagina.addVinculoDireitaApartirEsquerda(paginasNaoVinculado.removeAt(0))
                } else {
                    pagina.addVinculoDireita(paginasNaoVinculado.removeAt(0))
                    if (paginasNaoVinculado.isEmpty()) break
                    pagina.addVinculoDireitaApartirEsquerda(paginasNaoVinculado.removeAt(0))
                }
            }
            naoVinculado.clear()
            if (paginasNaoVinculado.isNotEmpty())
                naoVinculado.addAll(paginasNaoVinculado)
        }
    }

    private var qtde: Int = 0
    fun ordenarPaginaSimples() {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty()) return
        val existeImagem: Boolean = vinculado.stream().anyMatch { it.isImagemDupla }
        if (existeImagem) {
            qtde = 0
            vinculado.forEach { if (it.isImagemDupla) qtde++ }
            for (i in vinculado.size - 1 downTo vinculado.size - 1 - qtde)
                addNaoVinculado(vinculado[i])
            var processado: Int = qtde
            for (i in vinculado.size - 1 downTo 0) {
                val pagina: VinculoPagina = vinculado[i - processado]
                if (pagina.isImagemDupla) {
                    vinculado[i].addVinculoEsquerdaApartirDireita(pagina)
                    pagina.limparVinculadoDireita()
                    processado--
                } else vinculado[i].addVinculoEsquerda(pagina)
                if (processado <= 0) break
            }
        }
    }

    @JvmOverloads
    fun autoReordenarPaginaDupla(isLimpar: Boolean = false) {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty()) return
        var temImagemDupla = false
        if (isLimpar)
            ordenarPaginaSimples()
        else
            temImagemDupla = vinculado.stream().anyMatch { it.isImagemDupla }
        if (!temImagemDupla && isLimpar) {
            val lastIndex: Int = vinculado.size - 1
            for (pagina in vinculado) {
                val index: Int = vinculado.indexOf(pagina)
                if (pagina.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA || index >= lastIndex)
                    continue

                if (pagina.isOriginalPaginaDupla && pagina.isVinculadoEsquerdaPaginaDupla)
                    continue

                if (pagina.isOriginalPaginaDupla) {
                    val proximo: VinculoPagina = vinculado[index + 1]
                    if (proximo.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA || proximo.isVinculadoEsquerdaPaginaDupla)
                        continue
                    pagina.addVinculoDireitaApartirEsquerda(proximo)
                    proximo.limparVinculado()
                    for (idxNext in index + 1 until vinculado.size) {
                        if (idxNext < index + 1 || idxNext >= lastIndex) continue
                        val aux: VinculoPagina = vinculado[idxNext + 1]
                        proximo.addVinculoEsquerda(aux)
                        aux.limparVinculadoEsquerda()
                    }
                } else if (pagina.isVinculadoEsquerdaPaginaDupla) {
                    if (vinculado[index + 1].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA)
                        continue

                    var indexEmpty: Int = lastIndex
                    for (i in index + 1 until lastIndex) {
                        if (vinculado[i].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                            indexEmpty = i
                            break
                        }
                    }
                    addNaoVinculado(vinculado[indexEmpty])
                    for (i in indexEmpty downTo index + 2) {
                        val aux: VinculoPagina = vinculado[i - 1]
                        vinculado[i].addVinculoEsquerda(aux)
                        aux.limparVinculadoEsquerda()
                    }
                }
            }
        }
    }

    fun autoReordenarPHash(precisao: Double) {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty()) return
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        val temImagemDupla: Boolean = vinculado.stream().anyMatch { it.isImagemDupla }
        if (temImagemDupla) ordenarPaginaSimples()
        val processar: MutableList<VinculoPagina> = vinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = false) }
            .collect(Collectors.toList())
        processar.addAll(
            vinculado.parallelStream().filter { vp -> vp.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA }
                .map { vp -> VinculoPagina(vp, false, isNaoVinculado = false) }.collect(Collectors.toList())
        )
        processar.addAll(naoVinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, true, isNaoVinculado = false) }.collect(Collectors.toList())
        )
        val vinculadoTemp: List<VinculoPagina> = vinculado.parallelStream().map { vp -> VinculoPagina(vp) }
            .collect(Collectors.toList())
        val pHash = ImagePHash()
        val limiar = precisao / 10
        for (pagina in vinculadoTemp) {
            if (pagina.originalPHash.isNotEmpty()) {
                // Filtra apenas paginas que não foram encontradas, então pega os itens que são
                // parecidos e realizam uma validação do mais parecido dentre eles para retornar
                // o que mais se encaixa
                val vinculo: Optional<Pair<Int, VinculoPagina>> = processar.parallelStream()
                    .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
                    .filter { vp -> pHash.match(pagina.originalPHash, vp.vinculadoEsquerdaPHash, limiar) }
                    .map { vp ->
                        Pair<Int, VinculoPagina>(
                            pHash.matchLimiar(pagina.originalPHash, vp.vinculadoEsquerdaPHash, limiar),
                            vp
                        )
                    }
                    .filter { vp -> vp.first <= ImagePHash.SIMILAR } // Somente imagens semelhantes
                    .sorted { o1, o2 -> o2.first.compareTo(o1.first) }.findFirst()
                if (vinculo.isPresent) {
                    pagina.addVinculoEsquerda(vinculo.get().second)
                    vinculo.get().second.limparVinculadoEsquerda()
                }
            }
        }
        val naoLocalizado: List<VinculoPagina> = processar.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = true) }.collect(Collectors.toList())
        processar.addAll(
            vinculado.parallelStream().filter { vp -> vp.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA }
                .map { vp -> VinculoPagina(vp, false, isNaoVinculado = true) }.collect(Collectors.toList())
        )
        processar.addAll(naoVinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = true) }.collect(Collectors.toList())
        )
        vinculado.clear()
        naoVinculado.clear()
        vinculado.addAll(vinculadoTemp)
        naoVinculado.addAll(naoLocalizado)
    }

    fun autoReordenarHistogram(precisao: Double) {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty()) return
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        val temImagemDupla: Boolean = vinculado.stream().anyMatch { it.isImagemDupla }
        if (temImagemDupla) ordenarPaginaSimples()
        val processar: MutableList<VinculoPagina> = vinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, true, isNaoVinculado = false) }.collect(Collectors.toList())
        processar.addAll(
            vinculado.parallelStream().filter { vp -> vp.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA }
                .map { vp -> VinculoPagina(vp, isEsquerda = false, isNaoVinculado = false) }.collect(Collectors.toList())
        )
        processar.addAll(naoVinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, true, isNaoVinculado = false) }.collect(Collectors.toList())
        )
        val vinculadoTemp: List<VinculoPagina> = vinculado.parallelStream().map { vp -> VinculoPagina(vp) }
            .collect(Collectors.toList())
        val histogram = ImageHistogram()
        val limiar = precisao / 100
        for (pagina in vinculadoTemp) {
            if (pagina.originalPHash.isNotEmpty()) {
                // Filtra apenas paginas que não foram encontradas, então pega os itens que são
                // parecidos e realizam uma validação do mais parecido dentre eles para retornar
                // o que mais se encaixa
                val vinculo: Optional<Pair<Double, VinculoPagina>> = processar.parallelStream()
                    .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
                    .filter { vp ->
                        histogram.match(
                            pagina.originalHistogram, vp.vinculadoEsquerdaHistogram,
                            limiar
                        )
                    }
                    .map { vp ->
                        Pair<Double, VinculoPagina>(
                            histogram.matchLimiar(
                                pagina.originalHistogram,
                                vp.vinculadoEsquerdaHistogram,
                                limiar
                            ), vp
                        )
                    }
                    .sorted { o1, o2 -> o2.first.compareTo(o1.first) }.findFirst()
                if (vinculo.isPresent) {
                    pagina.addVinculoEsquerda(vinculo.get().second)
                    vinculo.get().second.limparVinculadoEsquerda()
                }
            }
        }
        val naoLocalizado: List<VinculoPagina> = processar.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = true) }.collect(Collectors.toList())
        processar.addAll(
            vinculado.parallelStream().filter { vp -> vp.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA }
                .map { vp -> VinculoPagina(vp, isEsquerda = false, isNaoVinculado = true) }.collect(Collectors.toList())
        )
        processar.addAll(naoVinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = true) }.collect(Collectors.toList())
        )
        vinculado.clear()
        naoVinculado.clear()
        vinculado.addAll(vinculadoTemp)
        naoVinculado.addAll(naoLocalizado)
    }

    fun autoReordenarInteligente(precisao: Double) {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty()) return
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        val temImagemDupla: Boolean = vinculado.stream().anyMatch { it.isImagemDupla }
        if (temImagemDupla) ordenarPaginaSimples()
        val processar: MutableList<VinculoPagina> = vinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = false) }.collect(Collectors.toList())
        processar.addAll(
            vinculado.parallelStream().filter { vp -> vp.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA }
                .map { vp -> VinculoPagina(vp, isEsquerda = false, isNaoVinculado = false) }.collect(Collectors.toList())
        )
        processar.addAll(naoVinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, true, isNaoVinculado = false) }.collect(Collectors.toList())
        )
        val vinculadoTemp: List<VinculoPagina> = vinculado.parallelStream().map { vp -> VinculoPagina(vp) }
            .collect(Collectors.toList())
        val pHash = ImagePHash()
        val histogram = ImageHistogram()
        val limiar = precisao / 100
        for (pagina in vinculadoTemp) {
            if (pagina.originalPHash.isNotEmpty()) {
                val capitulo = Utils.getCapitulo(pagina.originalPathPagina)
                val busca: MutableList<VinculoPagina> = mutableListOf()
                if (capitulo != null)
                    busca.addAll(processar.parallelStream()
                    .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
                    .filter { vp ->
                        val cap = Utils.getCapitulo(vp.vinculadoEsquerdaPathPagina)!!
                        capitulo.key.equals(cap.key) && capitulo.value == cap.value
                    }.collect(Collectors.toList())
                ) else
                    busca.addAll(processar.parallelStream()
                    .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
                    .collect(Collectors.toList()))
                val vinculo: Optional<Pair<Double, VinculoPagina>> = busca.parallelStream()
                    .filter { vp ->
                        histogram.match(
                            pagina.originalHistogram,
                            vp.vinculadoEsquerdaHistogram,
                            limiar
                        )
                    }
                    .map { vp ->
                        Pair<Double, VinculoPagina>(
                            histogram.matchLimiar(
                                pagina.originalHistogram,
                                vp.vinculadoEsquerdaHistogram,
                                limiar
                            ), vp
                        )
                    }
                    .map { vp ->
                        Pair(
                            pHash.matchLimiar(
                                pagina.originalPHash,
                                vp.second.vinculadoEsquerdaPHash, 1000.0
                            ).toDouble(),
                            vp.second
                        )
                    }
                    .sorted { o1, o2 -> o1.first.compareTo(o2.first) }.findFirst()
                if (vinculo.isPresent) {
                    pagina.addVinculoEsquerda(vinculo.get().second)
                    vinculo.get().second.limparVinculadoEsquerda()
                }
            }
        }
        val naoLocalizado: List<VinculoPagina> = processar.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, isEsquerda = true, isNaoVinculado = true) }.collect(Collectors.toList())
        processar.addAll(
            vinculado.parallelStream().filter { vp -> vp.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA }
                .map { vp -> VinculoPagina(vp, false, isNaoVinculado = true) }.collect(Collectors.toList())
        )
        processar.addAll(naoVinculado.parallelStream()
            .filter { vp -> vp.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA }
            .map { vp -> VinculoPagina(vp, true, isNaoVinculado = true) }.collect(Collectors.toList())
        )
        vinculado.clear()
        naoVinculado.clear()
        vinculado.addAll(vinculadoTemp)
        naoVinculado.addAll(naoLocalizado)
    }

    private var numeroPagina: Int = 0
    fun reordenarPeloNumeroPagina() {
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        if (vinculado == null || vinculado.isEmpty())
            return

        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        val vinculadoTemp: MutableList<VinculoPagina> = mutableListOf()
        val naoVinculadoTemp: MutableList<VinculoPagina> = mutableListOf()
        var maxNumPag: Int = 0

        for (pagina in vinculado) {
            if (pagina.vinculadoEsquerdaPagina > maxNumPag)
                maxNumPag = pagina.vinculadoEsquerdaPagina
            if (pagina.vinculadoDireitaPagina > maxNumPag)
                maxNumPag = pagina.vinculadoDireitaPagina
        }

        for (pagina in naoVinculado) {
            if (pagina.vinculadoEsquerdaPagina > maxNumPag)
                maxNumPag = pagina.vinculadoEsquerdaPagina
        }

        for (pagina in vinculado) {
            if (pagina.originalPagina === VinculoPagina.PAGINA_VAZIA)
                continue

            if (pagina.originalPagina > maxNumPag)
                vinculadoTemp.add(VinculoPagina(pagina))
            else {
                val novo = VinculoPagina(pagina)
                vinculadoTemp.add(novo)
                var encontrado: Optional<VinculoPagina> = vinculado.stream()
                    .filter { (it.vinculadoEsquerdaPagina.compareTo(pagina.originalPagina) === 0 || it.vinculadoDireitaPagina.compareTo(pagina.originalPagina) === 0) }
                    .findFirst()
                if (encontrado.isEmpty)
                    encontrado = naoVinculado.stream()
                    .filter { it.vinculadoEsquerdaPagina.compareTo(pagina.originalPagina) === 0 }
                    .findFirst()
                if (encontrado.isPresent) {
                    if (encontrado.get().vinculadoDireitaPagina === pagina.originalPagina)
                        novo.addVinculoEsquerdaApartirDireita(encontrado.get())
                    else
                        novo.addVinculoEsquerda(encontrado.get())
                }
            }
        }
        if (maxNumPag >= vinculadoTemp.size) {
            numeroPagina = naoVinculado.size - 1
            while (numeroPagina <= maxNumPag) {
                var encontrado: Optional<VinculoPagina> = vinculado.stream()
                    .filter { (it.vinculadoEsquerdaPagina.compareTo(numeroPagina) === 0 || it.vinculadoDireitaPagina.compareTo(numeroPagina) === 0) }
                    .findFirst()

                if (encontrado.isEmpty)
                    encontrado = naoVinculado.stream().filter { it.vinculadoEsquerdaPagina.compareTo(numeroPagina) === 0 }.findFirst()

                if (encontrado.isPresent) {
                    val item: VinculoPagina = encontrado.get()
                    if (item.vinculadoEsquerdaPagina === numeroPagina)
                        naoVinculadoTemp.add(VinculoPagina(item, isEsquerda = true, isNaoVinculado = true))
                    else
                        naoVinculadoTemp.add(VinculoPagina(item, false, isNaoVinculado = true))
                }
                numeroPagina++
            }
        }
        vinculado.clear()
        naoVinculado.clear()
        //conferir
        //vinculadoTemp.sortBy { a: VinculoPagina, b: VinculoPagina -> a.originalPagina.compareTo(b.originalPagina) }
        //naoVinculadoTemp.sortBy { a: VinculoPagina, b: VinculoPagina -> a.vinculadoEsquerdaPagina.compareTo(b.vinculadoEsquerdaPagina) }
        vinculado.addAll(vinculadoTemp)
        naoVinculadoTemp.addAll(naoVinculadoTemp)
    }

    // -------------------------------------------------------------------------------------------------
    fun addNaoVInculado(pagina: VinculoPagina, origem: Pagina) {
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        if (origem === Pagina.VINCULADO_DIREITA) {
            if (pagina.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA) 
                naoVinculado.add(VinculoPagina(pagina, false, isNaoVinculado = true))
            pagina.limparVinculadoDireita()
        } else {
            if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) 
                naoVinculado.add(VinculoPagina(pagina, isEsquerda = true, isNaoVinculado = true))
            
            if (pagina.isImagemDupla) 
                pagina.moverDireitaParaEsquerda() 
            else 
                pagina.limparVinculadoEsquerda()
        }
    }

    fun fromNaoVinculado(origem: VinculoPagina?, destino: VinculoPagina?, local: Pagina?) {
        if (origem == null || destino == null) 
            return
        
        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        val destinoIndex: Int = vinculado.indexOf(destino)
        val limite: Int = vinculado.size - 1
        naoVinculado.remove(origem)
        if (destino.imagemVinculadoEsquerda == null)
            vinculado[destinoIndex].addVinculoEsquerda(origem)
        else {
            addNaoVinculado(vinculado[limite])
            for (i in limite downTo destinoIndex) {
                if (i === destinoIndex)
                    vinculado[i].addVinculoEsquerda(origem)
                else
                    vinculado[i].addVinculoEsquerda(vinculado[i - 1])
            }
        }
    }

    fun onMovimentaEsquerda(origem: VinculoPagina?, destino: VinculoPagina?) {
        if (origem == null || destino == null || origem == destino)
            return

        val vinculado: ObservableList<VinculoPagina> = listener.vinculados
        val origemIndex: Int = vinculado.indexOf(origem)
        val destinoIndex: Int = vinculado.indexOf(destino)
        var diferenca: Int = destinoIndex - origemIndex
        if (origemIndex > destinoIndex) {
            var limite: Int = vinculado.size - 1
            var index: Int = vinculado.indexOf(vinculado.stream().filter { it.imagemVinculadoEsquerda == null }.findFirst().get())
            
            if (index < 0) index = limite
            for (i in index downTo origemIndex)
                if (vinculado[i].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA)
                    limite = i

            for (i in destinoIndex until origemIndex)
                addNaoVinculado(vinculado[i])

            diferenca *= -1
            for (i in destinoIndex until limite) {
                if (i === destinoIndex)
                    vinculado[i].addVinculoEsquerda(origem)
                else if (i + diferenca > limite)
                    continue
                else {
                    vinculado[i].addVinculoEsquerda(vinculado[i + diferenca])
                    vinculado[i + diferenca].limparVinculadoEsquerda()
                }
            }
            for (i in destinoIndex until limite)
                if (vinculado[i].isImagemDupla && vinculado[i].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA && vinculado[i].vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA)
                vinculado[i].moverDireitaParaEsquerda()
        } else {
            var limite: Int = vinculado.size - 1
            var espacos: Int = 0
            for (i in origemIndex until limite)
                if (vinculado[i].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA)
                    espacos++

            if (diferenca > espacos) {
                for (i in limite downTo limite - diferenca)
                    addNaoVinculado(vinculado[i])
                for (i in limite downTo origemIndex) {
                    if (i < destinoIndex)
                        vinculado[i].limparVinculadoEsquerda()
                    else
                        vinculado[i].addVinculoEsquerda(vinculado[i - diferenca])
                }
            } else {
                espacos = 0
                for (i in origemIndex until limite) {
                    if (vinculado[i].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                        espacos++
                        if (espacos >= diferenca) {
                            limite = i
                            break
                        }
                    }
                }
                espacos = 0
                var index: Int
                for (i in limite downTo origemIndex) {
                    if (i < destinoIndex) vinculado[i].limparVinculadoEsquerda(true) else {
                        index = i - (1 + espacos)
                        if (vinculado[index].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                            do {
                                espacos++
                                index = i - (1 + espacos)
                            } while (vinculado[index].vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA)
                        }
                        vinculado[i].addVinculoEsquerda(vinculado[index])
                    }
                }
            }
        }
    }

    fun onMovimentaDireita(origem: Pagina, itemOrigem: VinculoPagina?, destino: Pagina, itemDestino: VinculoPagina?) {
        if (itemOrigem == null || itemDestino == null || itemOrigem === itemDestino && destino === Pagina.VINCULADO_DIREITA)
            return

        val naoVinculado: ObservableList<VinculoPagina> = listener.naoVinculados
        if (destino !== Pagina.VINCULADO_ESQUERDA && itemDestino.isImagemDupla)
            naoVinculado.add(VinculoPagina(itemDestino, false, isNaoVinculado = true))
        else if (destino === Pagina.VINCULADO_ESQUERDA && itemDestino.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
            naoVinculado.add(VinculoPagina(itemDestino, isEsquerda = true, isNaoVinculado = true))
        if (origem === Pagina.VINCULADO_DIREITA && destino === Pagina.VINCULADO_DIREITA) {
            itemDestino.addVinculoDireita(itemOrigem)
            itemOrigem.limparVinculadoDireita()
        } else if (origem === Pagina.NAO_VINCULADO || destino === Pagina.NAO_VINCULADO) {
            if (origem === Pagina.NAO_VINCULADO && destino === Pagina.NAO_VINCULADO)
                return
            else if (origem === Pagina.NAO_VINCULADO) {
                itemDestino.addVinculoDireitaApartirEsquerda(itemOrigem)
                naoVinculado.remove(itemOrigem)
            } else if (destino === Pagina.NAO_VINCULADO)
                itemOrigem.limparVinculadoDireita()
        } else {
            val vinculado: ObservableList<VinculoPagina> = listener.vinculados
            var origemnIndex: Int = vinculado.indexOf(itemOrigem)
            var destinoIndex: Int = vinculado.indexOf(itemDestino)
            if (origem !== Pagina.VINCULADO_DIREITA && destino === Pagina.VINCULADO_ESQUERDA)
                itemDestino.addVinculoEsquerda(itemOrigem)
            else if (origem !== Pagina.VINCULADO_DIREITA && destino !== Pagina.VINCULADO_ESQUERDA)
                itemDestino.addVinculoDireitaApartirEsquerda(itemOrigem)
            else if (origem === Pagina.VINCULADO_DIREITA && destino === Pagina.VINCULADO_ESQUERDA)
                itemDestino.addVinculoEsquerda(itemOrigem)
            else if (origem === Pagina.VINCULADO_DIREITA && destino !== Pagina.VINCULADO_ESQUERDA)
                itemDestino.addVinculoDireitaApartirEsquerda(itemOrigem)

            val movido: Boolean = when (origem) {
                Pagina.VINCULADO_ESQUERDA -> itemOrigem.limparVinculadoEsquerda(true)
                Pagina.VINCULADO_DIREITA -> {
                    itemOrigem.limparVinculadoDireita()
                    false
                }
                else -> false
            }
            if (origemnIndex > destinoIndex && origem !== Pagina.VINCULADO_DIREITA && !movido)
                origemnIndex++
            destinoIndex++
            if (origemnIndex >= vinculado.size || destinoIndex >= vinculado.size)
                return
            val proximoOrigem: VinculoPagina = vinculado[origemnIndex]
            val proximoDestino: VinculoPagina = vinculado[destinoIndex]
            if (proximoDestino.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
                onMovimentaEsquerda(proximoOrigem, proximoDestino)
        }
    }

    // -------------------------------------------------------------------------------------------------
    fun findPagina(vinculado: ObservableList<VinculoPagina>, naoVinculado: ObservableList<VinculoPagina>, numeroPagina: Int): VinculoPagina? {
        if (numeroPagina === VinculoPagina.PAGINA_VAZIA) return null
        var pagina: Optional<VinculoPagina> = vinculado.stream()
            .filter {
                (it.vinculadoEsquerdaPagina.compareTo(numeroPagina) === 0 || it.vinculadoDireitaPagina.compareTo(numeroPagina) === 0)
            }
            .findFirst()
        return if (pagina.isPresent)
            pagina.get()
        else {
            pagina = naoVinculado.stream().filter { it.vinculadoEsquerdaPagina.compareTo(numeroPagina) === 0 }.findFirst()
            pagina.get()
        }
    }

    fun findPagina(paginas: MutableList<MangaPagina>, encontrados: MutableList<MangaPagina>, path: String, nomePagina: String, numeroPagina: Int, hash: String): MangaPagina? {
        var manga: MangaPagina? = null
        if (paginas.isEmpty())
            return manga

        val capitulo = Utils.getCapitulo(path)
        if (capitulo != null) {
            var encontrado: Optional<MangaPagina> = paginas.stream().filter { pg -> !encontrados.contains(pg) }
                .filter { pg -> pg.capitulo != null && pg.capitulo.compareTo(capitulo.key) === 0 }
                .filter { pg -> pg.hash != null && pg.hash.equals(hash, true) }.findFirst()

            if (encontrado.isPresent && encontrado.get().hash.isNotEmpty())
                manga = encontrado.get()

            if (manga == null) {
                encontrado = paginas.stream().filter { pg -> !encontrados.contains(pg) }
                    .filter { pg -> pg.capitulo != null && pg.capitulo.compareTo(capitulo.key) === 0 }
                    .filter { pg -> pg.nomePagina != null && pg.nomePagina.equals(nomePagina, true) }
                    .findFirst()

                if (encontrado.isPresent)
                    manga = encontrado.get()
            }

            if (manga == null) {
                encontrado = paginas.stream().filter { pg -> !encontrados.contains(pg) }
                    .filter { pg -> pg.capitulo != null && pg.capitulo.compareTo(capitulo.key) === 0 }
                    .filter { pg -> pg.numero != null && pg.numero.compareTo(numeroPagina) === 0 }
                    .findFirst()
                if (encontrado.isPresent)
                    manga = encontrado.get()
            }
        } else {
            var encontrado: Optional<MangaPagina> = paginas.stream().filter { pg -> !encontrados.contains(pg) }
                .filter { pg -> pg.hash != null && pg.hash.equals(hash, true) }.findFirst()
            if (encontrado.isPresent && encontrado.get().hash.isNotEmpty())
                manga = encontrado.get()

            if (manga == null) {
                encontrado = paginas.stream().filter { pg -> !encontrados.contains(pg) }
                    .filter { pg -> pg.nomePagina != null && pg.nomePagina.equals(nomePagina, true) }
                    .findFirst()

                if (encontrado.isPresent)
                    manga = encontrado.get()
            }

            if (manga == null) {
                encontrado = paginas.stream().filter { pg -> !encontrados.contains(pg) }
                    .filter { pg -> pg.numero != null && pg.numero.compareTo(numeroPagina) === 0 }
                    .findFirst()
                if (encontrado.isPresent)
                    manga = encontrado.get()
            }
        }
        if (manga != null)
            encontrados.add(manga)
        return manga
    }

}