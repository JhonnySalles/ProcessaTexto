package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Database
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*


interface VocabularioDao : RepositoryDao<UUID?, Vocabulario> {
    val tipo: Database

    @Throws(SQLException::class)
    fun save(obj: Vocabulario)

    @Throws(SQLException::class)
    fun insertManual(obj: Vocabulario)

    @Throws(SQLException::class)
    fun updateManual(obj: Vocabulario)

    @Throws(SQLException::class)
    fun delete(obj: Vocabulario)

    fun exist(vocabulario: String): Boolean

    @Throws(SQLException::class)
    fun select(vocabulario: String, base: String): Optional<Vocabulario>

    @Throws(SQLException::class)
    fun select(vocabulario: String): Optional<Vocabulario>

    @Throws(SQLException::class)
    fun select(id: UUID?): Optional<Vocabulario>

    @Throws(SQLException::class)
    fun selectAll(): MutableList<Vocabulario>

    @Throws(SQLException::class)
    fun insertExclusao(palavra: String)

    @Throws(SQLException::class)
    fun existeExclusao(palavra: String, basico: String): Boolean

    @Throws(SQLException::class)
    fun selectExclusao(): Set<String>

    @Throws(SQLException::class)
    fun selectEnvioVocabulario(ultimo: LocalDateTime): List<Vocabulario>

    @Throws(SQLException::class)
    fun selectExclusaoEnvio(ultimo: LocalDateTime): List<String>
}