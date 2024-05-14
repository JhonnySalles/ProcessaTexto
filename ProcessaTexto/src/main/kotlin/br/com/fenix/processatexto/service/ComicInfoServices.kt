package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.ComicInfoDao
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import java.sql.SQLException
import java.util.*


class ComicInfoServices {
    private val comicInfoDao: ComicInfoDao = DaoFactory.createComicInfoDao()

    @Throws(SQLException::class)
    fun select(comic: String, linguagem: String): Optional<ComicInfo> = comicInfoDao.select(comic, linguagem)

    @Throws(SQLException::class)
    fun save(comic: ComicInfo) {
        val saved = select(comic.comic!!, comic.languageISO!!)
        if (saved.isEmpty || saved.get().getId() == null)
            comicInfoDao.insert(comic)
        else {
            comic.setId(saved.get().getId())
            comicInfoDao.update(comic)
        }
    }
}