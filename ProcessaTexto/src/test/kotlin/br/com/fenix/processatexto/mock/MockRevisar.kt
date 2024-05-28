package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.random.Random


class MockRevisar(var conexao: Conexao) : MockJpaBase<UUID?, Revisar>() {

    companion object {
        const val KANJI = "出遭っ"
        const val WORD = "Cat"
    }

    override fun mockEntity(): Revisar = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: Revisar): Revisar = updateEntityById(input.getId())

    override fun updateEntityById(lastId: UUID?): Revisar {
        val vocabulario = if (conexao == Conexao.TEXTO_INGLES) WORD else "$KANJI---"
        val ingles = if (conexao == Conexao.TEXTO_INGLES) "" else "To meet (by chance), to come across, to run across." + "---"
        val formaBasica = if (conexao == Conexao.TEXTO_INGLES) "" else "出遭う" + "---"
        val leituraNovel = if (conexao == Conexao.TEXTO_INGLES) "" else "デアッ" + "---"
        val portugues = if (conexao == Conexao.TEXTO_INGLES) "Gato." else "Conhecer (acaso), se deparar com, atravessar."
        return Revisar(
            lastId, vocabulario + Random.nextInt().toString() + "---", formaBasica, "デアッ" + "---", leituraNovel,
            "$portugues---", ingles, 1, isRevisado = true, isAnime = true, isManga = true, isNovel = true
        )
    }

    override fun mockEntity(id: UUID?): Revisar {
        val vocabulario = if (conexao == Conexao.TEXTO_INGLES) WORD else KANJI
        val ingles = if (conexao == Conexao.TEXTO_INGLES) "" else "To meet (by chance), to come across, to run across."
        val formaBasica = if (conexao == Conexao.TEXTO_INGLES) "" else "出遭う"
        val leituraNovel = if (conexao == Conexao.TEXTO_INGLES) "" else "デアッ"
        val portugues = if (conexao == Conexao.TEXTO_INGLES) "Gato." else "Conhecer (acaso), se deparar com, atravessar."
        return Revisar(
            id, vocabulario + Random.nextInt().toString(), formaBasica, "デアッ", leituraNovel,
            "$portugues---", ingles, 999999999, isRevisado = false, isAnime = false, isManga = false, isNovel = false
        )
    }

    override fun assertsService(input: Revisar?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.vocabulario.isNotEmpty())
        assertTrue(input.portugues.isNotEmpty())

        if (conexao != Conexao.TEXTO_INGLES) {
            assertTrue(input.formaBasica.isNotEmpty())
            assertTrue(input.leitura.isNotEmpty())
            assertTrue(input.leituraNovel.isNotEmpty())
            assertTrue(input.ingles.isNotEmpty())
        }
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