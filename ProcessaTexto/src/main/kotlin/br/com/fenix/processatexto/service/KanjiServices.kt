package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.KanjiDao
import br.com.fenix.processatexto.model.entities.processatexto.Kanji
import java.sql.SQLException
import java.util.*


class KanjiServices {

    private val kanjiDao: KanjiDao = DaoFactory.createKanjiDao()

    @Throws(SQLException::class)
    fun select(kanji: String): Optional<Kanji> = kanjiDao.select(kanji)

}