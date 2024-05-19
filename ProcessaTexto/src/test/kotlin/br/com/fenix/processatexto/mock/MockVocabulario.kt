package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.random.Random


class MockVocabulario(var conexao: Conexao) : MockJpaBase<UUID?, Vocabulario>() {

    override fun mockEntity(): Vocabulario = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: Vocabulario): Vocabulario = updateEntityById(input.getId())

    override fun updateEntityById(lastId: UUID?): Vocabulario {
        val ingles = if (conexao == Conexao.TEXTO_INGLES) "" else "ingles" + "---"
        val formaBasica = if (conexao == Conexao.TEXTO_INGLES) "" else "formaBasica" + "---"
        val leituraNovel = if (conexao == Conexao.TEXTO_INGLES) "" else "leituraNovel" + "---"
        return Vocabulario(
            lastId, "vocabulario" + Random.nextInt().toString() + "---", formaBasica, "leitura" + "---", leituraNovel,
            ingles, "portugues" + "---"
        )
    }

    override fun mockEntity(id: UUID?): Vocabulario {
        val ingles = if (conexao == Conexao.TEXTO_INGLES) "" else "ingles"
        val formaBasica = if (conexao == Conexao.TEXTO_INGLES) "" else "formaBasica"
        val leituraNovel = if (conexao == Conexao.TEXTO_INGLES) "" else "leituraNovel"
        return Vocabulario(
            id, "vocabulario" + Random.nextInt().toString(), formaBasica, "leitura", leituraNovel, ingles, "portugues"
        )
    }

    override fun assertsService(input: Vocabulario?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.vocabulario.isNotEmpty())
        assertTrue(input.formaBasica.isNotEmpty())
        assertTrue(input.leitura.isNotEmpty())
        assertTrue(input.leituraNovel.isNotEmpty())
        assertTrue(input.portugues.isNotEmpty())
        assertTrue(input.ingles.isNotEmpty())
    }

    override fun assertsService(oldObj: Vocabulario?, newObj: Vocabulario?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.vocabulario, newObj!!.vocabulario)
        assertEquals(oldObj.formaBasica, newObj.formaBasica)
        assertEquals(oldObj.leitura, newObj.leitura)
        assertEquals(oldObj.leituraNovel, newObj.leituraNovel)
        assertEquals(oldObj.portugues, newObj.portugues)
        assertEquals(oldObj.ingles, newObj.ingles)
    }

}