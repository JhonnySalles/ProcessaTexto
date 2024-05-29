package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapa
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapitulo
import br.com.fenix.processatexto.model.entities.novelextractor.NovelTexto
import br.com.fenix.processatexto.model.entities.novelextractor.NovelVolume
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import org.junit.jupiter.api.Assertions.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*


class MockNovel : MockBase<UUID?, NovelVolume>() {

    companion object {
        val LINGUAGEM = Language.PORTUGUESE
    }

    override fun mockEntity(): NovelVolume = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntityById(lastId: UUID?): NovelVolume = updateEntity(mockEntity(randomId()))

    override fun updateEntity(input: NovelVolume): NovelVolume {
        input.novel += "---"
        input.volume = 2f
        input.capitulo = 2f
        input.lingua = Language.ENGLISH
        input.arquivo += "---"

        input.capa?.let {
            it.novel += "---"
            it.volume = 2f
            it.lingua = Language.ENGLISH
            it.arquivo += "---"
            it.extenssao = "JPG"
        }

        input.vocabularios.first().let { v ->
            v.palavra += "---"
            v.portugues += "---"
            v.ingles += "---"
            v.leitura += "---"
            v.leituraNovel += "---"
        }

        input.capitulos.first().let { c ->
            c.novel += "---"
            c.volume = 2f
            c.capitulo = 2f
            c.descricao += "---"

            c.vocabularios.first().let { v ->
                v.palavra += "---"
                v.portugues += "---"
                v.ingles += "---"
                v.leitura += "---"
                v.leituraNovel += "---"
            }

            c.textos.first().let { p ->
                p.texto += "---"
                p.sequencia = 2
            }
        }

        return input
    }

    override fun mockEntity(id: UUID?): NovelVolume {
        val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.BLACK
        graphics.clearRect(0, 0, image.width, image.height)
        val capa = NovelCapa(UUID.randomUUID(), "manga", 1f, LINGUAGEM, "arquivo", "jpg", image)

        val texto = NovelTexto(id, "texto", 1)

        var idVocab = if (id != null) UUID.fromString(id.toString().substring(0, 35).plus("1")) else UUID.randomUUID()
        var vocabulario = VocabularioExterno(idVocab, "vocabulario capitulo", "português capitulo", "ingles capitulo", "leitura capitulo", "leitura novel capitulo", true)
        val capitulo = NovelCapitulo(id, "novel", 1f, 1f, "descricao", 1, LINGUAGEM, mutableListOf(texto), mutableSetOf(vocabulario))

        idVocab = if (id != null) UUID.fromString(id.toString().substring(0, 35).plus("2")) else UUID.randomUUID()
        vocabulario = VocabularioExterno(idVocab, "vocabulario novel", "português novel", "ingles novel", "leitura novel", "leitura novel novel", true)

        return NovelVolume(
            id, "novel", "titulo", "titulo alternativo", "série", "descrição",
            "arquivo", "editora", "autor", 1f, LINGUAGEM, capa, isFavorito = false,
            mutableListOf(capitulo), mutableSetOf(vocabulario)
        )
    }

    override fun assertsService(input: NovelVolume?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.novel.isNotEmpty())
        assertTrue(input.volume > 0)
        assertTrue(input.arquivo.isNotEmpty())
        assertTrue(input.titulo.isNotEmpty())
        assertTrue(input.tituloAlternativo.isNotEmpty())
        assertTrue(input.serie.isNotEmpty())
        assertTrue(input.descricao.isNotEmpty())
        assertTrue(input.autor.isNotEmpty())
        assertTrue(input.editora.isNotEmpty())
        assertNotNull(input.lingua)

        assertNotNull(input.capa)
        input.capa?.let {
            assertTrue(it.novel.isNotEmpty())
            assertTrue(it.volume > 0)
            assertNotNull(it.lingua)
            assertTrue(it.arquivo.isNotEmpty())
            assertTrue(it.extenssao.isNotEmpty())
        }

        assertTrue(input.vocabularios.isNotEmpty())
        input.vocabularios.first().let { v ->
            assertTrue(v.palavra.isNotEmpty())
            assertTrue(v.portugues.isNotEmpty())
            assertTrue(v.ingles.isNotEmpty())
            assertTrue(v.leitura.isNotEmpty())
            if (v !is VocabularioExterno)
                assertTrue(v.leituraNovel.isNotEmpty())
        }

        assertTrue(input.capitulos.isNotEmpty())
        input.capitulos.first().let { c ->
            assertTrue(c.novel.isNotEmpty())
            assertTrue(c.volume > 0)
            assertTrue(c.capitulo > 0)
            assertTrue(c.descricao.isNotEmpty())
            assertTrue(c.sequencia > 0)

            c.vocabularios.first().let { v ->
                assertTrue(v.palavra.isNotEmpty())
                assertTrue(v.portugues.isNotEmpty())
                assertTrue(v.ingles.isNotEmpty())
                assertTrue(v.leitura.isNotEmpty())
                if (v !is VocabularioExterno)
                    assertTrue(v.leituraNovel.isNotEmpty())
            }

            c.textos.first().let { t ->
                assertTrue(t.texto.isNotEmpty())
                assertTrue(t.sequencia > 0)
            }
        }
    }

    override fun assertsService(oldObj: NovelVolume?, newObj: NovelVolume?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.novel, newObj!!.novel)
        assertEquals(oldObj.volume, newObj.volume)
        assertEquals(oldObj.capitulo, newObj.capitulo)
        assertEquals(oldObj.titulo, newObj.titulo)
        assertEquals(oldObj.tituloAlternativo, newObj.tituloAlternativo)
        assertEquals(oldObj.serie, newObj.serie)
        assertEquals(oldObj.descricao, newObj.descricao)
        assertEquals(oldObj.arquivo, newObj.arquivo)
        assertEquals(oldObj.editora, newObj.editora)
        assertEquals(oldObj.autor, newObj.autor)
        assertEquals(oldObj.lingua, newObj.lingua)
        assertEquals(oldObj.vocabularios.size, newObj.vocabularios.size)
        assertEquals(oldObj.capitulos.size, newObj.capitulos.size)

        val capaOld = oldObj.capa
        val capaNew = newObj.capa
        assertNotNull(capaOld)
        assertNotNull(capaNew)
        assertEquals(capaOld!!.novel, capaNew!!.novel)
        assertEquals(capaOld.volume, capaNew.volume)
        assertEquals(capaOld.lingua, capaNew.lingua)
        assertEquals(capaOld.arquivo, capaNew.arquivo)
        assertEquals(capaOld.extenssao, capaNew.extenssao)

        var vocabOld = oldObj.vocabularios.first()
        var vocabNew = newObj.vocabularios.first()
        assertEquals(vocabOld.palavra, vocabNew.palavra)
        assertEquals(vocabOld.portugues, vocabNew.portugues)
        assertEquals(vocabOld.ingles, vocabNew.ingles)
        assertEquals(vocabOld.leitura, vocabNew.leitura)
        if (vocabOld !is VocabularioExterno)
            assertEquals(vocabOld.leituraNovel, vocabNew.leituraNovel)

        val capituloOld = oldObj.capitulos.first()
        val capituloNew = newObj.capitulos.first()
        assertEquals(capituloOld.novel, capituloNew.novel)
        assertEquals(capituloOld.volume, capituloNew.volume)
        assertEquals(capituloOld.capitulo, capituloNew.capitulo)
        assertEquals(capituloOld.descricao, capituloNew.descricao)
        assertEquals(capituloOld.sequencia, capituloNew.sequencia)
        assertEquals(capituloOld.vocabularios.size, capituloNew.vocabularios.size)
        assertEquals(capituloOld.textos.size, capituloNew.textos.size)

        vocabOld = capituloOld.vocabularios.first()
        vocabNew = capituloNew.vocabularios.first()
        assertEquals(vocabOld.palavra, vocabNew.palavra)
        assertEquals(vocabOld.portugues, vocabNew.portugues)
        assertEquals(vocabOld.ingles, vocabNew.ingles)
        assertEquals(vocabOld.leitura, vocabNew.leitura)
        if (vocabOld !is VocabularioExterno)
            assertEquals(vocabOld.leituraNovel, vocabNew.leituraNovel)

        val textoOld = capituloOld.textos.first()
        val textoNew = capituloNew.textos.first()
        assertEquals(textoOld.texto, textoNew.texto)
        assertEquals(textoOld.sequencia, textoNew.sequencia)
    }

}