package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.EntityBase
import java.lang.reflect.Field


abstract class MockJpaBase<ID, E : EntityBase<ID, E>> : MockJpa<ID, E> {

    override fun mockEntity(): E = mockEntity(null)

    abstract fun randomId(): ID?

    override fun mockEntityList(): List<E> {
        val list: MutableList<E> = mutableListOf()
        for (i in 1..3)
            list.add(mockEntity(null))
        return list
    }

    override fun updateList(list: List<E>): List<E> {
        val updated: MutableList<E> = mutableListOf()
        for (item in list)
            updated.add(updateEntity(item))
        return updated
    }

    override fun updateEntityById(lastId: ID?): E {
        val update = mockEntity(lastId)
        val fields: Array<Field> = update::class.java.declaredFields

        for (field in fields) {
            field.isAccessible = true
            when (field.type) {
                String::class.java -> field[update] = field[update].toString().plus("---")
                Int::class.java, Long::class.java -> field[update] = 100
                Float::class.java, Double::class.java -> field[update] = 100.0
                Char::class.java -> field[update] = '-'
            }

        }
        return update
    }

}