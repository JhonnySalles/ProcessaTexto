package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*

interface ComicInfoDao : RepositoryDao<UUID?, ComicInfo> {
    @Throws(SQLException::class)
    fun save(obj: ComicInfo)

    @Throws(SQLException::class)
    fun find(comic: String, linguagem: String): Optional<ComicInfo>

    @Throws(SQLException::class)
    fun find(id: UUID, comic: String, linguagem: String): Optional<ComicInfo>

    @Throws(SQLException::class)
    fun findEnvio(ultimo: LocalDateTime): List<ComicInfo>
}