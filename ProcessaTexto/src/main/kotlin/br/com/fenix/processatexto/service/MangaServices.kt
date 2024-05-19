package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaCapitulo
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaPagina
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*


class MangaServices {

    private val LOGGER = LoggerFactory.getLogger(MangaServices::class.java)

    private val mangaDao = DaoFactory.createMangaDao()

    @get:Throws(SQLException::class)
    val tabelas: List<String> get() = mangaDao!!.tabelas

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean): List<MangaTabela> = mangaDao!!.selectTabelas(todos)

    @Throws(SQLException::class)
    fun selectAll(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language?): List<MangaTabela> {
        return mangaDao!!.selectAll(base, manga, volume, capitulo, linguagem)
    }

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String): List<MangaTabela> {
        return mangaDao!!.selectTabelas(todos, isLike, base, linguagem, manga)
    }

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String, volume: Int, capitulo: Float): List<MangaTabela> {
        return mangaDao!!.selectTabelas(todos, isLike, base, linguagem, manga, volume, capitulo)
    }

    @Throws(SQLException::class)
    fun selectTabelasJson(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language, inverterTexto: Boolean): List<MangaTabela> {
        return mangaDao!!.selectTabelasJson(base, manga, volume, capitulo, linguagem, inverterTexto)
    }

    @Throws(SQLException::class)
    fun selectDadosTransferir(base: String, tabela: String): List<MangaVolume> = mangaDao!!.selectDadosTransferir(base, tabela)

    @Throws(SQLException::class)
    fun getTabelasTransferir(base: String, tabela: String): List<String> = mangaDao!!.getTabelasTransferir(base, tabela)

    @Throws(SQLException::class)
    fun updateCancel(base: String, obj: MangaVolume) = mangaDao!!.updateCancel(base, obj)

    @Throws(SQLException::class)
    fun insertDadosTransferir(base: String, volume: MangaVolume) {
        val idVolume: UUID = mangaDao!!.insertVolume(base, volume)
        for (capitulo in volume.capitulos) {
            val idCapitulo: UUID = mangaDao.insertCapitulo(base, idVolume, capitulo)
            for (pagina in capitulo.paginas) {
                val idPagina: UUID = mangaDao.insertPagina(base, idCapitulo, pagina)
                for (texto in pagina.textos)
                    mangaDao.insertTexto(base, idPagina, texto)
            }
        }
        if (volume.capa != null)
            mangaDao.insertCapa(base, idVolume, volume.capa!!)
    }

    @Throws(SQLException::class)
    fun updateVocabularioVolume(base: String, volume: MangaVolume) {
        insertVocabularios(base, volume.getId()!!, null, null, volume.vocabularios)
        mangaDao!!.updateProcessado(base, volume.getId()!!)
    }

    @Throws(SQLException::class)
    fun updateVocabularioCapitulo(base: String, capitulo: MangaCapitulo) = insertVocabularios(base, null, capitulo.getId(), null, capitulo.vocabularios)

    @Throws(SQLException::class)
    fun updateVocabularioPagina(base: String, pagina: MangaPagina) = insertVocabularios(base, null, null, pagina.getId(), pagina.vocabularios)

    @Throws(SQLException::class)
    fun insertVocabularios(base: String, idVolume: UUID?, idCapitulo: UUID?, idPagina: UUID?, vocabularios: Set<VocabularioExterno>) {
        mangaDao!!.insertVocabulario(base, idVolume, idCapitulo, idPagina, vocabularios)
    }

    @Throws(SQLException::class)
    fun createTabela(base: String) = mangaDao!!.createTabela(base)

    private var limpeza = true

    @Throws(SQLException::class)
    fun salvarAjustes(tabelas: ObservableList<MangaTabela>) {
        limpeza = true
        for (tabela in tabelas)
            for (volume in tabela.volumes) {
                if (limpeza && volume.lingua == Language.JAPANESE) {
                    limpeza = false
                    mangaDao!!.deletarVocabulario(tabela.base)
                }
                if (volume.getId() == null)
                    volume.setId(mangaDao!!.insertVolume(tabela.base, volume))
                else if (volume.isAlterado) {
                    if (volume.isItemExcluido) {
                        val aux: Optional<MangaVolume> = mangaDao!!.selectVolume(tabela.base, volume.getId()!!)
                        if (aux.isPresent) {
                            aux.get().capitulos.forEach { anterior ->
                                var existe = false
                                for (atual in volume.capitulos)
                                    if (atual.getId() == anterior.getId()) {
                                        existe = true
                                        break
                                    }
                                if (!existe)
                                    try {
                                        mangaDao.deleteCapitulo(tabela.base, anterior)
                                    } catch (e: SQLException) {
                                        LOGGER.error(e.message, e)
                                    }
                            }
                        }
                    }
                    mangaDao!!.updateVolume(tabela.base, volume)
                }
                if (volume.capitulos.isEmpty())
                    mangaDao!!.deleteVolume(tabela.base, volume)
                else
                    for (capitulo in volume.capitulos)
                        if (capitulo.isAlterado) {
                            if (capitulo.isItemExcluido) {
                                val aux: Optional<MangaCapitulo> = mangaDao!!.selectCapitulo(tabela.base, capitulo.getId()!!)
                                if (aux.isPresent) {
                                    aux.get().paginas.forEach { anterior ->
                                        var existe = false
                                        for (atual in capitulo.paginas)
                                            if (atual.getId() == anterior.getId()) {
                                                existe = true
                                                break
                                            }
                                        if (!existe)
                                            try {
                                                mangaDao.deletePagina(tabela.base, anterior)
                                            } catch (e: SQLException) {
                                                LOGGER.error(e.message, e)
                                            }
                                    }
                                }
                            }
                            for (pagina in capitulo.paginas)
                                if (pagina.isItemExcluido) {
                                    val aux: Optional<MangaPagina> = mangaDao!!.selectPagina(tabela.base, pagina.getId()!!)
                                    if (aux.isPresent) {
                                        aux.get().textos.forEach { anterior ->
                                            var existe = false
                                            for (atual in pagina.textos)
                                                if (atual.getId() == anterior.getId()) {
                                                    existe = true
                                                    break
                                                }
                                            if (!existe) try {
                                                mangaDao.deleteTexto(tabela.base, anterior)
                                            } catch (e: SQLException) {
                                                LOGGER.error(e.message, e)
                                            }
                                        }
                                    }
                                }
                            mangaDao!!.updateCapitulo(tabela.base, volume.getId()!!, capitulo)
                        }
            }
    }

    @Throws(SQLException::class)
    fun salvarTraducao(base: String, volume: MangaVolume) {
        mangaDao!!.deleteVolume(base, volume)
        insertDadosTransferir(base, volume)
    }

    @Throws(SQLException::class)
    fun getManga(base: String, manga: String, linguagem: Language, volume: Int): Optional<MangaVolume> = mangaDao!!.selectVolume(base, manga, volume, linguagem)

}
