package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Estatistica
import org.junit.jupiter.api.Assertions.*
import java.util.*


class MockEstatistica : MockJpaBase<UUID?, Estatistica>() {

    override fun mockEntity(): Estatistica = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: Estatistica): Estatistica {
        input.kanji += "---"
        input.tipo += "---"
        input.leitura += "---"
        input.quantidade *= 2
        input.percentual *= 2
        input.media *= 2
        input.percentMedia *= 2
        input.corSequencial *= 2

        return input
    }

    override fun updateEntityById(lastId: UUID?): Estatistica {
        return Estatistica(lastId, "kanji" + "---", "tipo" + "---", "leitura" + "---", 3.0, 22f, 20.0, 550f, 10)
    }

    override fun mockEntity(id: UUID?): Estatistica {
        return Estatistica(id, "kanji", "tipo", "leitura", 11.0, 12f, 10.0, 35f, 5)
    }

    override fun assertsService(input: Estatistica?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.kanji.isNotEmpty())
        assertTrue(input.tipo.isNotEmpty())
        assertTrue(input.leitura.isNotEmpty())
        assertTrue(input.quantidade > 0)
        assertTrue(input.media > 0)
        assertTrue(input.percentual > 0)
        assertTrue(input.percentMedia > 0)
        assertTrue(input.corSequencial > 0)
    }

    override fun assertsService(oldObj: Estatistica?, newObj: Estatistica?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.kanji, newObj!!.kanji)
        assertEquals(oldObj.tipo, newObj.tipo)
        assertEquals(oldObj.leitura, newObj.leitura)
        assertEquals(oldObj.quantidade, newObj.quantidade)
        assertEquals(oldObj.media, newObj.media)
        assertEquals(oldObj.percentual, newObj.percentual)
        assertEquals(oldObj.percentMedia, newObj.percentMedia)
        assertEquals(oldObj.corSequencial, newObj.corSequencial)
    }

}