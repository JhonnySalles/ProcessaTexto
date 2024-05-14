package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapitulo
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela
import org.jisho.textosJapones.model.entities.novelextractor.NovelTexto
import org.jisho.textosJapones.model.entities.novelextractor.NovelVolume
import java.sql.SQLException
import java.util.*


interface NovelDao {
    @Throws(SQLException::class)
    fun selectVolume(base: String, novel: String, volume: Int, linguagem: Language): Optional<NovelVolume>

    @Throws(SQLException::class)
    fun selectVolume(base: String, arquivo: String, linguagem: Language): Optional<NovelVolume>

    @Throws(SQLException::class)
    fun selectVolume(base: String, id: UUID): Optional<NovelVolume>

    @Throws(SQLException::class)
    fun selectCapitulo(base: String, id: UUID): Optional<NovelCapitulo>

    @Throws(SQLException::class)
    fun selectAll(base: String): MutableList<NovelTabela>

    @Throws(SQLException::class)
    fun selectAll(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language?): MutableList<NovelTabela>

    @Throws(SQLException::class)
    fun insertVolume(base: String, obj: NovelVolume): UUID

    @Throws(SQLException::class)
    fun insertCapitulo(base: String, idVolume: UUID, obj: NovelCapitulo): UUID

    @Throws(SQLException::class)
    fun insertTexto(base: String, idPagina: UUID, obj: NovelTexto): UUID

    @Throws(SQLException::class)
    fun deleteVolume(base: String, obj: NovelVolume)

    @Throws(SQLException::class)
    fun deleteVocabulario(base: String)

    @Throws(SQLException::class)
    fun updateCancel(base: String, obj: NovelVolume)

    @Throws(SQLException::class)
    fun insertVocabulario(base: String, idVolume: UUID?, idCapitulo: UUID?, vocabulario: MutableSet<VocabularioExterno>)

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language?, novel: String): MutableList<NovelTabela>

    @Throws(SQLException::class)
    fun updateProcessado(base: String, id: UUID)

    @Throws(SQLException::class)
    fun createTabela(base: String)

    @Throws(SQLException::class)
    fun selectTabela(base: String): String

    @get:Throws(SQLException::class)
    val tabelas: List<String>
}