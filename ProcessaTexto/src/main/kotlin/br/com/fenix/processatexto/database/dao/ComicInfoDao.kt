package br.com.fenix.processatexto.database.dao

import java.sql.SQLException
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import java.util.*


interface ComicInfoDao {
    @Throws(SQLException::class)
    fun insert(obj: ComicInfo)

    @Throws(SQLException::class)
    fun update(obj: ComicInfo)

    @Throws(SQLException::class)
    fun select(comic: String, linguagem: String): Optional<ComicInfo>
}