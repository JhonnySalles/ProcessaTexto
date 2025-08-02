package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.enums.Language
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.random.Random


class MockFilaSql : MockJpaBase<UUID?, FilaSQL>() {

    override fun mockEntity(): FilaSQL = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: FilaSQL): FilaSQL {
        input.sequencial = Random.nextLong(1, 10000)
        input.select += "---"
        input.update += "---"
        input.delete += "---"
        input.vocabulario += "---"
        input.linguagem = Language.ENGLISH
        input.isExporta = !input.isExporta
        input.isLimpeza = !input.isLimpeza

        return input
    }

    override fun updateEntityById(lastId: UUID?): FilaSQL {
        return FilaSQL(
            lastId, Random.nextLong(1, 10000), "nome" + "---","select_sql" + "---",
            "update_sql" + "---", "delete_sql" + "---", "vocabulario" + "---",
            Language.ENGLISH, isExporta = false, isLimpeza = true
        )
    }

    override fun mockEntity(id: UUID?): FilaSQL {
        return FilaSQL(
            id, 0, "nome","select_sql",
            "update_sql", "delete_sql", "vocabulario",
            Language.PORTUGUESE, isExporta = true, isLimpeza = false
        )
    }

    override fun assertsService(input: FilaSQL?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.select.isNotEmpty())
        assertTrue(input.update.isNotEmpty())
        assertTrue(input.delete.isNotEmpty())
        assertTrue(input.vocabulario.isNotEmpty())
        assertNotNull(input.linguagem)
    }

    override fun assertsService(oldObj: FilaSQL?, newObj: FilaSQL?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.select, newObj!!.select)
        assertEquals(oldObj.update, newObj.update)
        assertEquals(oldObj.delete, newObj.delete)
        assertEquals(oldObj.vocabulario, newObj.vocabulario)
        assertEquals(oldObj.linguagem, newObj.linguagem)
        assertEquals(oldObj.isExporta, newObj.isExporta)
        assertEquals(oldObj.isLimpeza, newObj.isLimpeza)
    }

}