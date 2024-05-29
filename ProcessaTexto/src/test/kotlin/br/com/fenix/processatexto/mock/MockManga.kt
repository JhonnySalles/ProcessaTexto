package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import org.checkerframework.checker.units.qual.g
import org.junit.jupiter.api.Assertions.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*


class MockManga : MockBase<UUID?, MangaVolume>() {

    companion object {
        val LINGUAGEM = Language.PORTUGUESE
    }

    override fun mockEntity(): MangaVolume = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntityById(lastId: UUID?): MangaVolume = updateEntity(mockEntity(randomId()))

    override fun updateEntity(input: MangaVolume): MangaVolume {
        input.manga += "---"
        input.volume = 2
        input.capitulo = 2f
        input.lingua = Language.ENGLISH
        input.arquivo += "---"

        input.capa?.let {
            it.manga += "---"
            it.volume = 2
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
            c.manga += "---"
            c.volume = 2
            c.capitulo = 2f
            c.scan += "---"
            c.isExtra = false
            c.isRaw = false

            c.vocabularios.first().let { v ->
                v.palavra += "---"
                v.portugues += "---"
                v.ingles += "---"
                v.leitura += "---"
                v.leituraNovel += "---"
            }

            c.paginas.first().let { p ->
                p.nomePagina += "---"
                p.numero = 2
                p.hash += "---"

                p.vocabularios.first().let { v ->
                    v.palavra += "---"
                    v.portugues += "---"
                    v.ingles += "---"
                    v.leitura += "---"
                    v.leituraNovel += "---"
                }

                p.textos.first().let { t ->
                    t.texto += "---"
                    t.x1 = 2
                    t.x2 = 2
                    t.y1 = 3
                    t.y2 = 3
                }
            }
        }

        return input
    }

    override fun mockEntity(id: UUID?): MangaVolume {
        val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.BLACK
        graphics.clearRect(0, 0, image.width, image.height)
        val capa = MangaCapa(UUID.randomUUID(), "manga", 1, LINGUAGEM, "arquivo", "jpg", image)

        val texto = MangaTexto(id, "texto", 1, 1, 1, 2, 2)

        var idVocab : UUID? = if (id != null) UUID.fromString(id.toString().substring(0, 36).plus("1")) else UUID.randomUUID()
        var vocabulario = VocabularioExterno(idVocab, "vocabulario pagina", "português pagina", "ingles pagina", "leitura pagina", "leitura novel pagina", true)
        val pagina = MangaPagina(id, "nome", 1, "hash_pagina", mutableListOf(texto), mutableSetOf(vocabulario))

        idVocab = if (id != null) UUID.fromString(id.toString().substring(0, 35).plus("2")) else UUID.randomUUID()
        vocabulario = VocabularioExterno(idVocab, "vocabulario capitulo", "português capitulo", "ingles capitulo", "leitura capitulo", "leitura novel capitulo", true)
        val capitulo = MangaCapitulo(id, "manga", 1, 1f, LINGUAGEM, "scan", extra = true, raw = true, vocabularios = mutableSetOf(vocabulario), paginas = mutableListOf(pagina))

        idVocab = if (id != null) UUID.fromString(id.toString().substring(0, 35).plus("3")) else UUID.randomUUID()
        vocabulario = VocabularioExterno(idVocab, "vocabulario manga", "português manga", "ingles manga", "leitura manga", "leitura novel manga", true)
        return MangaVolume(
            id, "manga", 1, LINGUAGEM, "arquivo",
            mutableSetOf(vocabulario), mutableListOf(capitulo), capa
        )
    }

    override fun assertsService(input: MangaVolume?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.manga.isNotEmpty())
        assertTrue(input.volume > 0)
        assertTrue(input.arquivo.isNotEmpty())
        assertNotNull(input.lingua)

        assertNotNull(input.capa)
        input.capa?.let {
            assertTrue(it.manga.isNotEmpty())
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
            assertTrue(c.manga.isNotEmpty())
            assertTrue(c.volume > 0)
            assertTrue(c.capitulo > 0)
            assertTrue(c.scan.isNotEmpty())

            c.vocabularios.first().let { v ->
                assertTrue(v.palavra.isNotEmpty())
                assertTrue(v.portugues.isNotEmpty())
                assertTrue(v.ingles.isNotEmpty())
                assertTrue(v.leitura.isNotEmpty())
                if (v !is VocabularioExterno)
                    assertTrue(v.leituraNovel.isNotEmpty())
            }

            c.paginas.first().let { p ->
                assertTrue(p.nomePagina.isNotEmpty())
                assertTrue(p.numero > 0)
                assertTrue(p.hash.isNotEmpty())

                p.vocabularios.first().let { v ->
                    assertTrue(v.palavra.isNotEmpty())
                    assertTrue(v.portugues.isNotEmpty())
                    assertTrue(v.ingles.isNotEmpty())
                    assertTrue(v.leitura.isNotEmpty())
                    if (v !is VocabularioExterno)
                        assertTrue(v.leituraNovel.isNotEmpty())
                }

                p.textos.first().let { t ->
                    assertTrue(t.texto.isNotEmpty())
                    assertTrue(t.x1 > 0)
                    assertTrue(t.x2 > 0)
                    assertTrue(t.y1 > 0)
                    assertTrue(t.y2 > 0)
                }
            }
        }
    }

    override fun assertsService(oldObj: MangaVolume?, newObj: MangaVolume?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.manga, newObj!!.manga)
        assertEquals(oldObj.volume, newObj.volume)
        assertEquals(oldObj.capitulo, newObj.capitulo)
        assertEquals(oldObj.arquivo, newObj.arquivo)
        assertEquals(oldObj.lingua, newObj.lingua)
        assertEquals(oldObj.vocabularios.size, newObj.vocabularios.size)
        assertEquals(oldObj.capitulos.size, newObj.capitulos.size)

        val capaOld = oldObj.capa
        val capaNew = newObj.capa
        assertNotNull(capaOld)
        assertNotNull(capaNew)
        assertEquals(capaOld!!.manga, capaNew!!.manga)
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
        assertEquals(capituloOld.manga, capituloNew.manga)
        assertEquals(capituloOld.volume, capituloNew.volume)
        assertEquals(capituloOld.capitulo, capituloNew.capitulo)
        assertEquals(capituloOld.scan, capituloNew.scan)
        assertEquals(capituloOld.isExtra, capituloNew.isExtra)
        assertEquals(capituloOld.isRaw, capituloNew.isRaw)
        assertEquals(capituloOld.vocabularios.size, capituloNew.vocabularios.size)
        assertEquals(capituloOld.paginas.size, capituloNew.paginas.size)

        vocabOld = capituloOld.vocabularios.first()
        vocabNew = capituloNew.vocabularios.first()
        assertEquals(vocabOld.palavra, vocabNew.palavra)
        assertEquals(vocabOld.portugues, vocabNew.portugues)
        assertEquals(vocabOld.ingles, vocabNew.ingles)
        assertEquals(vocabOld.leitura, vocabNew.leitura)
        if (vocabOld !is VocabularioExterno)
            assertEquals(vocabOld.leituraNovel, vocabNew.leituraNovel)

        val paginaOld = capituloOld.paginas.first()
        val paginaNew = capituloNew.paginas.first()
        assertEquals(paginaOld.nomePagina, paginaNew.nomePagina)
        assertEquals(paginaOld.numero, paginaNew.numero)
        assertEquals(paginaOld.hash, paginaNew.hash)
        assertEquals(paginaOld.textos.size, paginaNew.textos.size)

        vocabOld = paginaOld.vocabularios.first()
        vocabNew = paginaNew.vocabularios.first()
        assertEquals(vocabOld.palavra, vocabNew.palavra)
        assertEquals(vocabOld.portugues, vocabNew.portugues)
        assertEquals(vocabOld.ingles, vocabNew.ingles)
        assertEquals(vocabOld.leitura, vocabNew.leitura)
        if (vocabOld !is VocabularioExterno)
            assertEquals(vocabOld.leituraNovel, vocabNew.leituraNovel)

        val textoOld = paginaOld.textos.first()
        val textoNew = paginaNew.textos.first()
        assertEquals(textoOld.texto, textoNew.texto)
        assertEquals(textoOld.x1, textoNew.x1)
        assertEquals(textoOld.x2, textoNew.x2)
        assertEquals(textoOld.y1, textoNew.y1)
        assertEquals(textoOld.y2, textoNew.y2)
    }

}