package br.com.fenix.processatexto.database

import br.com.fenix.processatexto.database.dao.*
import br.com.fenix.processatexto.database.dao.implement.*
import br.com.fenix.processatexto.model.enums.Conexao
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object DaoFactory {

    private val LOGGER: Logger = LoggerFactory.getLogger(DaoFactory::class.java)

    fun createVocabularioJaponesDao(): VocabularioDao = VocabularioJaponesDaoJDBC(Conexao.TEXTO_JAPONES)

    fun createVocabularioInglesDao(): VocabularioDao = VocabularioInglesDaoJDBC(Conexao.TEXTO_INGLES)

    fun createEstatisticaDao(): EstatisticaDao = EstatisticaDaoJDBC(Conexao.TEXTO_JAPONES)

    fun createRevisarJaponesDao(): RevisarDao = RevisarJaponesDaoJDBC(Conexao.TEXTO_JAPONES)

    fun createRevisarInglesDao(): RevisarDao = RevisarInglesDaoJDBC(Conexao.TEXTO_INGLES)

    fun createLegendasDao(): LegendasDao? {
        val dados = JdbcFactory.getConfiguracao(Conexao.DECKSUBTITLE)
        return if (dados.isPresent) LegendasDaoJDBC(Conexao.DECKSUBTITLE, dados.get().base) else null
    }

    fun createMangaDao(): MangaDao? {
        val dados = JdbcFactory.getConfiguracao(Conexao.MANGA_EXTRACTOR)
        return if (dados.isPresent) MangaDaoJDBC(Conexao.MANGA_EXTRACTOR, dados.get().base) else null
    }

    fun createNovelDao(): NovelDao? {
        val dados = JdbcFactory.getConfiguracao(Conexao.NOVEL_EXTRACTOR)
        return if (dados.isPresent) NovelDaoJDBC(Conexao.NOVEL_EXTRACTOR, dados.get().base) else null
    }

    fun createVincularDao(): VincularDao? {
        val dados = JdbcFactory.getConfiguracao(Conexao.NOVEL_EXTRACTOR)
        return if (dados.isPresent) VincularDaoJDBC(Conexao.NOVEL_EXTRACTOR) else null
    }

    fun createKanjiDao(): KanjiDao = KanjiDaoJDBC(Conexao.TEXTO_JAPONES)

    fun createComicInfoDao(): ComicInfoDao = ComicInfoJDBC(Conexao.PROCESSA_TEXTO)

    fun createSincronizacaoDao(): SincronizacaoDao = SincronizacaoDaoJDBC(Conexao.PROCESSA_TEXTO)

    val vocabularioExternos: List<VocabularioDao>
        get() {
            val externos: MutableList<VocabularioDao> = mutableListOf()

            if (JdbcFactory.getConfiguracao(Conexao.MANGA_EXTRACTOR).isPresent)
                externos.add(VocabularioExternoDaoJDBC(Conexao.MANGA_EXTRACTOR))

            if (JdbcFactory.getConfiguracao(Conexao.NOVEL_EXTRACTOR).isPresent)
                externos.add(VocabularioExternoDaoJDBC(Conexao.NOVEL_EXTRACTOR))

            return externos
        }
}