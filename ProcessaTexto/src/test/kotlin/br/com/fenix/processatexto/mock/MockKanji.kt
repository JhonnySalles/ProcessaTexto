package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Kanji
import org.junit.jupiter.api.Assertions.*
import java.util.*


class MockKanji : MockJpaBase<UUID?, Kanji>() {

    override fun mockEntity(): Kanji = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: Kanji): Kanji = updateEntityById(input.getId())

    override fun updateEntityById(lastId: UUID?): Kanji {
        return Kanji(lastId, "kanji" + "---", "palavra" + "---", "significado" + "---")
    }

    override fun mockEntity(id: UUID?): Kanji {
        return Kanji(id, "kanji", "palavra", "significado")
    }

    override fun assertsService(input: Kanji?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.kanji.isNotEmpty())
        assertTrue(input.palavra.isNotEmpty())
        assertTrue(input.significado.isNotEmpty())
    }

    override fun assertsService(oldObj: Kanji?, newObj: Kanji?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.kanji, newObj!!.kanji)
        assertEquals(oldObj.palavra, newObj.palavra)
        assertEquals(oldObj.significado, newObj.significado)
    }

}