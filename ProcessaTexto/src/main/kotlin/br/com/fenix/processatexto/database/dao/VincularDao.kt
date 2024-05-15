package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVinculo
import br.com.fenix.processatexto.model.entities.processatexto.Vinculo
import br.com.fenix.processatexto.model.enums.Language
import java.sql.SQLException
import java.util.*


interface VincularDao : RepositoryDao<UUID?, Vinculo> {
    @Throws(SQLException::class)
    fun update(base: String, obj: Vinculo)

    @Throws(SQLException::class)
    fun select(base: String, id: UUID): Optional<Vinculo>

    @Throws(SQLException::class)
    fun select(
        base: String,
        volume: Int,
        mangaOriginal: String,
        original: Language?,
        arquivoOriginal: String,
        mangaVinculado: String,
        vinculado: Language?,
        arquivoVinculado: String
    ): Optional<Vinculo>

    @Throws(SQLException::class)
    fun select(base: String, volume: Int, mangaOriginal: String, arquivoOriginal: String, mangaVinculado: String, arquivoVinculado: String): Optional<Vinculo>

    @Throws(SQLException::class)
    fun select(base: String, volume: Int, mangaOriginal: String, original: Language, mangaVinculado: String, vinculado: Language): Optional<Vinculo>

    @Throws(SQLException::class)
    fun delete(base: String, obj: Vinculo)

    @Throws(SQLException::class)
    fun insert(base: String, obj: Vinculo): UUID

    @Throws(SQLException::class)
    fun createTabelas(nome: String): Boolean

    @Throws(SQLException::class)
    fun getMangas(base: String, linguagem: Language): List<String>

    @get:Throws(SQLException::class)
    val tabelas: List<String>

    @Throws(SQLException::class)
    fun selectVinculo(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language): MutableList<MangaVinculo>

    @Throws(SQLException::class)
    fun selectTabelasJson(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language): MutableList<MangaTabela>
}