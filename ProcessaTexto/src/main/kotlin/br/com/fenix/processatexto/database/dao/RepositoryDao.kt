package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.EntityBase
import java.util.*


interface RepositoryDao<ID, E : EntityBase<ID, E>> {

    fun beginTransaction()
    fun commit()
    fun rollBack()

    fun query(@org.intellij.lang.annotations.Language("sql") sql: String, isThrowsNotUpdate : Boolean = true) : String?
    fun query(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any?>, isThrowsNotUpdate : Boolean = true) : String?
    fun queryEntity(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any?>): Optional<E>
    fun queryList(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any?>): List<E>

    fun insert(obj: E) : ID
    fun update(obj: E)
    fun delete(id: ID, column: String = "id")

    fun find(id: ID, column: String = "id"): Optional<E>
    fun findAll(): List<E>

}