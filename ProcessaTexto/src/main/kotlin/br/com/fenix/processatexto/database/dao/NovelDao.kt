package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapa
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapitulo
import br.com.fenix.processatexto.model.entities.novelextractor.NovelTexto
import br.com.fenix.processatexto.model.entities.novelextractor.NovelVolume
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela
import java.sql.SQLException
import java.util.*


interface NovelDao {

    @Throws(SQLException::class)
    fun selectAll(base: String, novel: String, volume: Int, linguagem: Language): MutableList<NovelVolume>

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
    fun selectAll(base: String, novel: String, volume: Int, capitulo: Float, linguagem: Language?): MutableList<NovelTabela>

    @Throws(SQLException::class)
    fun insertCapa(base: String, idVolume: UUID, obj: NovelCapa): UUID

    @Throws(SQLException::class)
    fun insertVolume(base: String, obj: NovelVolume): UUID

    @Throws(SQLException::class)
    fun insertCapitulo(base: String, idVolume: UUID, obj: NovelCapitulo): UUID

    @Throws(SQLException::class)
    fun insertTexto(base: String, idPagina: UUID, obj: NovelTexto): UUID

    @Throws(SQLException::class)
    fun updateVolume(base: String, obj: NovelVolume)

    @Throws(SQLException::class)
    fun updateCapitulo(base: String, obj: NovelCapitulo)

    @Throws(SQLException::class)
    fun updateTexto(base: String, obj: NovelTexto)

    @Throws(SQLException::class)
    fun updateCapa(base: String, obj: NovelCapa)

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

    @Throws(SQLException::class)
    fun deleteTabela(base: String)

    @get:Throws(SQLException::class)
    val tabelas: List<String>
}