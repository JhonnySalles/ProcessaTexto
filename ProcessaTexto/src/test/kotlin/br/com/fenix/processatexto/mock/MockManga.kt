package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import org.junit.jupiter.api.Assertions.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO


class MockManga : MockBase<UUID?, MangaVolume>() {

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
            it.extenssao += "---"
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
        val input = ByteArrayInputStream(ByteArray(1))
        val image: BufferedImage = ImageIO.read(input)
        val capa = MangaCapa(UUID.randomUUID(), "manga", 1, Language.PORTUGUESE, "arquivo", "extensao", image)

        val texto = MangaTexto(UUID.randomUUID(), "texto", 1, 1, 1, 2, 2)

        var vocabulario = VocabularioExterno(UUID.randomUUID(), "vocabulario pagina", "forma_basica pagina", "ingles pagina", "leitura pagina", "leitura_novel pagina", true)
        val pagina = MangaPagina(UUID.randomUUID(), "nome", 1, "hash_pagina", mutableListOf(texto), mutableSetOf(vocabulario))

        vocabulario = VocabularioExterno(UUID.randomUUID(), "vocabulario capitulo", "forma_basica capitulo", "ingles capitulo", "leitura capitulo", "leitura_novel capitulo", true)
        val capitulo = MangaCapitulo(UUID.randomUUID(), "manga", 1, 1f, Language.PORTUGUESE, "scan", true, true, mutableSetOf(vocabulario), mutableListOf(pagina))

        vocabulario = VocabularioExterno(UUID.randomUUID(), "vocabulario manga", "forma_basica manga", "ingles manga", "leitura manga", "leitura_novel manga", true)
        return MangaVolume(
            UUID.randomUUID(), "manga", 1, Language.PORTUGUESE, "arquivo",
            mutableSetOf(vocabulario), mutableListOf(capitulo), capa
        )
    }

    override fun assertsService(input: MangaVolume?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.manga.isNotEmpty())
        assertTrue(input.volume > 0)
        assertTrue(input.capitulo > 0)
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