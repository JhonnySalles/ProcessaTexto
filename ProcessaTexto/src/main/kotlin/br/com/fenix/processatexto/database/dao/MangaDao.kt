package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import java.sql.SQLException
import java.util.*


interface MangaDao {
    @Throws(SQLException::class)
    fun updateVolume(base: String, obj: MangaVolume)

    @Throws(SQLException::class)
    fun updateCapitulo(base: String, obj: MangaCapitulo)

    @Throws(SQLException::class)
    fun updateCapitulo(base: String, IdVolume: UUID, obj: MangaCapitulo)

    @Throws(SQLException::class)
    fun updatePagina(base: String, obj: MangaPagina)

    @Throws(SQLException::class)
    fun updateTexto(base: String, obj: MangaTexto)

    @Throws(SQLException::class)
    fun updateCapa(base: String, obj: MangaCapa)

    @Throws(SQLException::class)
    fun selectVolume(base: String, manga: String, volume: Int, linguagem: Language): Optional<MangaVolume>

    @Throws(SQLException::class)
    fun selectVolume(base: String, id: UUID): Optional<MangaVolume>

    @Throws(SQLException::class)
    fun selectCapitulo(base: String, id: UUID): Optional<MangaCapitulo>

    @Throws(SQLException::class)
    fun selectPagina(base: String, id: UUID): Optional<MangaPagina>

    @Throws(SQLException::class)
    fun selectCapa(base: String, id: UUID): Optional<MangaCapa>

    @Throws(SQLException::class)
    fun selectAll(base: String): MutableList<MangaTabela>

    @Throws(SQLException::class)
    fun selectAll(base: String?, manga: String, volume: Int, capitulo: Float, linguagem: Language?): MutableList<MangaTabela>

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean): List<MangaTabela>

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String): List<MangaTabela>

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String, volume: Int): List<MangaTabela>

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String, volume: Int, capitulo: Float): List<MangaTabela>

    @Throws(SQLException::class)
    fun selectTabelasJson(base: String?, manga: String, volume: Int, capitulo: Float, linguagem: Language, inverterTexto: Boolean): List<MangaTabela>

    @Throws(SQLException::class)
    fun updateCancel(base: String, obj: MangaVolume)

    @Throws(SQLException::class)
    fun insertVolume(base: String, obj: MangaVolume): UUID

    @Throws(SQLException::class)
    fun insertCapitulo(base: String, idVolume: UUID, obj: MangaCapitulo): UUID

    @Throws(SQLException::class)
    fun insertPagina(base: String, idCapitulo: UUID, obj: MangaPagina): UUID

    @Throws(SQLException::class)
    fun insertTexto(base: String, idPagina: UUID, obj: MangaTexto): UUID

    @Throws(SQLException::class)
    fun insertCapa(base: String, idVolume: UUID, obj: MangaCapa): UUID

    @Throws(SQLException::class)
    fun deleteVolume(base: String, obj: MangaVolume)

    @Throws(SQLException::class)
    fun deleteCapitulo(base: String, obj: MangaCapitulo)

    @Throws(SQLException::class)
    fun deletePagina(base: String, obj: MangaPagina)

    @Throws(SQLException::class)
    fun deleteTexto(base: String, obj: MangaTexto)

    @Throws(SQLException::class)
    fun deleteCapa(base: String, obj: MangaCapa)

    @Throws(SQLException::class)
    fun deletarVocabulario(base: String)

    @Throws(SQLException::class)
    fun updateProcessado(base: String, id: UUID)

    @Throws(SQLException::class)
    fun insertVocabulario(base: String, idVolume: UUID?, idCapitulo: UUID?, idPagina: UUID?, vocabulario: Set<VocabularioExterno>)

    @Throws(SQLException::class)
    fun selectDadosTransferir(base: String, tabela: String): MutableList<MangaVolume>

    @Throws(SQLException::class)
    fun getTabelasTransferir(base: String, tabela: String): MutableList<String>

    @Throws(SQLException::class)
    fun createTabela(base: String)

    @Throws(SQLException::class)
    fun deleteTabela(base: String)

    @get:Throws(SQLException::class)
    val tabelas: List<String>
}