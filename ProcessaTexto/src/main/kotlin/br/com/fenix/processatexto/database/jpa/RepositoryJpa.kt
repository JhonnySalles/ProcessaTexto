package br.com.fenix.processatexto.database.jpa

import br.com.fenix.processatexto.model.entities.EntityBase
import java.util.*
import jakarta.transaction.Transactional


interface RepositoryJpa<ID, E : EntityBase<ID, E>> {

    fun beginTransaction()
    fun commit()
    fun rollBack()

    fun exists(entity: E): Boolean

    fun find(id: ID): Optional<E>
    fun findAll(): List<E>

    @Transactional
    fun save(entity: E): E
    @Transactional
    fun saveAll(list: List<E>): List<E>

    @Transactional
    fun delete(entity: E)
    @Transactional
    fun delete(id: ID)

    fun queryEntity(@org.intellij.lang.annotations.Language("sql") sql : String) : Optional<E>
    fun queryList(@org.intellij.lang.annotations.Language("sql") sql: String) : List<E>

    fun queryEntity(@org.intellij.lang.annotations.Language("sql") sql : String, params : Map<String, Any>) : Optional<E>
    fun queryList(@org.intellij.lang.annotations.Language("sql") sql: String, params : Map<String, Any>) : List<E>

    fun query(@org.intellij.lang.annotations.Language("sql") sql : String)
    fun query(@org.intellij.lang.annotations.Language("sql") sql : String, params : Map<String, Any>)

}