package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*


interface ComicInfoDao {
    @Throws(SQLException::class)
    fun insert(obj: ComicInfo)

    @Throws(SQLException::class)
    fun update(obj: ComicInfo)

    @Throws(SQLException::class)
    fun select(comic: String, linguagem: String): Optional<ComicInfo>

    @Throws(SQLException::class)
    fun select(id: UUID, comic: String, linguagem: String): Optional<ComicInfo>

    @Throws(SQLException::class)
    fun selectEnvio(ultimo: LocalDateTime): List<ComicInfo>
}