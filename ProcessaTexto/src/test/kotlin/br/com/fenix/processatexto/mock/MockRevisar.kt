package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.Assertions.*
import java.util.*


class MockRevisar(var conexao: Conexao) : MockJpaBase<UUID?, Revisar>() {

    override fun mockEntity(): Revisar = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: Revisar): Revisar = updateEntityById(input.getId())

    override fun updateEntityById(lastId: UUID?): Revisar {
        val ingles = if (conexao == Conexao.TEXTO_INGLES) "" else "ingles" + "---"
        val formaBasica = if (conexao == Conexao.TEXTO_INGLES) "" else "formaBasica" + "---"
        val leituraNovel = if (conexao == Conexao.TEXTO_INGLES) "" else "leituraNovel" + "---"
        return Revisar(
            lastId, "vocabulario" + "---", formaBasica, "leitura" + "---", leituraNovel,
            "portugues" + "---", ingles, 9999, false, isAnime = false, isManga = false, isNovel = false
        )
    }

    override fun mockEntity(id: UUID?): Revisar {
        val ingles = if (conexao == Conexao.TEXTO_INGLES) "" else "ingles"
        val formaBasica = if (conexao == Conexao.TEXTO_INGLES) "" else "formaBasica"
        val leituraNovel = if (conexao == Conexao.TEXTO_INGLES) "" else "leituraNovel"
        return Revisar(
            id, "vocabulario", formaBasica, "leitura", leituraNovel, "portugues",
            ingles, 1, true, isAnime = true, isManga = true, isNovel = true
        )
    }

    override fun assertsService(input: Revisar?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.vocabulario.isNotEmpty())
        assertTrue(input.formaBasica.isNotEmpty())
        assertTrue(input.leitura.isNotEmpty())
        assertTrue(input.leituraNovel.isNotEmpty())
        assertTrue(input.portugues.isNotEmpty())
        assertTrue(input.ingles.isNotEmpty())
        assertTrue(input.isRevisado)
        assertTrue(input.isManga)
        assertTrue(input.isNovel)
        assertTrue(input.isAnime)
    }

    override fun assertsService(oldObj: Revisar?, newObj: Revisar?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.vocabulario, newObj!!.vocabulario)
        assertEquals(oldObj.formaBasica, newObj.formaBasica)
        assertEquals(oldObj.leitura, newObj.leitura)
        assertEquals(oldObj.leituraNovel, newObj.leituraNovel)
        assertEquals(oldObj.portugues, newObj.portugues)
        assertEquals(oldObj.ingles, newObj.ingles)
        assertEquals(oldObj.isRevisado, newObj.isRevisado)
        assertEquals(oldObj.isManga, newObj.isManga)
        assertEquals(oldObj.isNovel, newObj.isNovel)
        assertEquals(oldObj.isAnime, newObj.isAnime)
    }

}