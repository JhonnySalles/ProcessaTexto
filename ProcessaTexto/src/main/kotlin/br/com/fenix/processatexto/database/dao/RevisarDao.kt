package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Database
import java.sql.SQLException
import java.util.*


interface RevisarDao {
    val tipo: Database

    @Throws(SQLException::class)
    fun insert(obj: Revisar)

    @Throws(SQLException::class)
    fun update(obj: Revisar)

    @Throws(SQLException::class)
    fun delete(obj: Revisar)

    @Throws(SQLException::class)
    fun delete(vocabulario: String)
    fun exist(vocabulario: String): Boolean
    fun isValido(vocabulario: String): String

    @Throws(SQLException::class)
    fun select(vocabulario: String, base: String): Optional<Revisar>

    @Throws(SQLException::class)
    fun select(vocabulario: String): Optional<Revisar>

    @Throws(SQLException::class)
    fun select(id: UUID): Optional<Revisar>

    @Throws(SQLException::class)
    fun selectAll(): List<Revisar>

    @Throws(SQLException::class)
    fun selectFrases(select: String): List<String>

    @Throws(SQLException::class)
    fun selectTraduzir(quantidadeRegistros: Int): List<Revisar>

    @Throws(SQLException::class)
    fun selectQuantidadeRestante(): String

    @Throws(SQLException::class)
    fun selectRevisar(pesquisar: String, isAnime: Boolean, isManga: Boolean, isNovel: Boolean): Optional<Revisar>

    @Throws(SQLException::class)
    fun selectSimilar(vocabulario: String, ingles: String): MutableList<Revisar>

    @Throws(SQLException::class)
    fun incrementaVezesAparece(vocabulario: String)

    @Throws(SQLException::class)
    fun setIsManga(obj: Revisar)

    @Throws(SQLException::class)
    fun setIsNovel(obj: Revisar)
}