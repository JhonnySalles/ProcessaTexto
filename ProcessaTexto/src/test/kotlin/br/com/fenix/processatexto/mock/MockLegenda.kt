package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.subtitle.Legenda
import br.com.fenix.processatexto.model.enums.Language
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.random.Random


class MockLegenda : MockJpaBase<UUID?, Legenda>() {

    override fun mockEntity(): Legenda = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: Legenda): Legenda = updateEntityById(input.getId())

    override fun updateEntityById(lastId: UUID?): Legenda {
        return Legenda(
            lastId, Random.nextInt(10, 100), Random.nextInt(10, 100),
            Language.ENGLISH, "tempo" + "---",
            "texto" + "---", "traducao" + "---", "vocabulario" + "---",
            "", ""
        )
    }

    override fun mockEntity(id: UUID?): Legenda {
        return Legenda(
            id ?: randomId(), 1, 1, Language.PORTUGUESE, "tempo",
            "texto", "traducao", "vocabulario",
            "", ""
        )
    }

    override fun assertsService(input: Legenda?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.episodio > 0)
        assertTrue(input.tempo.isNotEmpty())
        assertTrue(input.texto.isNotEmpty())
        assertTrue(input.traducao.isNotEmpty())
        assertTrue(input.vocabulario.isNotEmpty())
        assertNotNull(input.linguagem)
    }

    override fun assertsService(oldObj: Legenda?, newObj: Legenda?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.episodio, newObj!!.episodio)
        assertEquals(oldObj.tempo, newObj.tempo)
        assertEquals(oldObj.texto, newObj.texto)
        assertEquals(oldObj.traducao, newObj.traducao)
        assertEquals(oldObj.vocabulario, newObj.vocabulario)
        assertEquals(oldObj.linguagem, newObj.linguagem)
    }

}