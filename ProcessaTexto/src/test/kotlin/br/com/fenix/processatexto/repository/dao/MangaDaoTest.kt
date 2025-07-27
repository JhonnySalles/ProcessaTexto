package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.MangaDao
import br.com.fenix.processatexto.mock.MockManga
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class MangaDaoTest {

    init {
        TestsConfig.prepareDatabase()
    }

    @InjectMocks
    private var repository: MangaDao? = DaoFactory.createMangaDao()

    private var input = MockManga()
    private val base = "teste_manga"

    private lateinit var lastEntity: MangaVolume

    @Test
    @Order(1)
    fun testCreate() {
        repository!!.createTabela(base)
    }

    @Test
    @Order(2)
    fun testInsert() {
        lastEntity = input.mockEntity()

        repository!!.insertVolume(base, lastEntity)

        val idVolume = lastEntity.getId()!!
        for (capitulo in lastEntity.capitulos) {
            repository!!.insertCapitulo(base, idVolume, capitulo)
            val idCapitulo: UUID = capitulo.getId()!!

            for (pagina in capitulo.paginas) {
                repository!!.insertPagina(base, idCapitulo, pagina)
                val idPagina: UUID = pagina.getId()!!

                for (texto in pagina.textos)
                    repository!!.insertTexto(base, idPagina, texto)
            }
        }

        if (lastEntity.capa != null)
            repository!!.insertCapa(base, idVolume, lastEntity.capa!!)

        input.assertsService(lastEntity)
    }

    @Test
    @Order(3)
    fun testUpdate() {
        lastEntity = input.updateEntity(lastEntity)

        repository!!.updateVolume(base, lastEntity)
        repository!!.updateVocabulario(lastEntity.vocabularios)
        for (capitulo in lastEntity.capitulos) {
            repository!!.updateCapitulo(base, lastEntity.getId()!!, capitulo)
            repository!!.updateVocabulario(capitulo.vocabularios)

            for (pagina in capitulo.paginas) {
                repository!!.updatePagina(base, pagina)
                repository!!.updateVocabulario(pagina.vocabularios)

                for (texto in pagina.textos)
                    repository!!.updateTexto(base, texto)
            }
        }

        if (lastEntity.capa != null)
            repository!!.updateCapa(base, lastEntity.capa!!)

        input.assertsService(lastEntity)
    }

    @Test
    @Order(4)
    fun testSelect() {
        var entity = repository!!.selectVolume(base, lastEntity.getId()!!)
        Assertions.assertTrue(entity.isPresent)
        input.assertsService(lastEntity, entity.get())
        lastEntity = entity.get()

        entity = repository!!.selectVolume(base, lastEntity.manga, lastEntity.volume, lastEntity.lingua)
        Assertions.assertTrue(entity.isPresent)

        val capitulo = lastEntity.capitulos[0]
        val cap = repository!!.selectCapitulo(base, capitulo.getId()!!)
        Assertions.assertTrue(cap.isPresent)

        val pagina = capitulo.paginas[0]
        val pag = repository!!.selectPagina(base, pagina.getId()!!)
        Assertions.assertTrue(pag.isPresent)
        Assertions.assertTrue(pag.get().textos.isNotEmpty())

        if (lastEntity.capa != null) {
            val capa = repository!!.selectCapa(base, lastEntity.capa!!.getId()!!)
            Assertions.assertTrue(capa.isPresent)
        }

        var list = repository!!.selectAll(base)
        Assertions.assertTrue(list.isNotEmpty())

        list = repository!!.selectAll(base, lastEntity.manga, lastEntity.volume, lastEntity.capitulo, lastEntity.lingua)
        Assertions.assertTrue(list.isNotEmpty())
    }

    @Test
    @Order(5)
    fun testProcessado() {
        repository!!.updateProcessado(base, lastEntity.getId()!!)
    }

    @Test
    @Order(6)
    fun testDelete() {
        val capa = lastEntity.capa
        val capitulo = lastEntity.capitulos[0]
        val pagina = capitulo.paginas[0]

        repository!!.deletarVocabulario(base)

        for (texto in pagina.textos)
            repository!!.deleteTexto(base, texto)

        repository!!.deletePagina(base, pagina)

        val pag = repository!!.selectPagina(base, pagina.getId()!!)
        Assertions.assertTrue(pag.isEmpty)

        repository!!.deleteCapitulo(base, capitulo)

        val cap = repository!!.selectCapitulo(base, capitulo.getId()!!)
        Assertions.assertTrue(cap.isEmpty)

        if (capa != null) {
            repository!!.deleteCapa(base, capa)
            val ca = repository!!.selectCapa(base, lastEntity.getId()!!)
            Assertions.assertTrue(ca.isEmpty)
        }
        repository!!.deleteVolume(base, lastEntity)

        val vol = repository!!.selectVolume(base, lastEntity.getId()!!)
        Assertions.assertTrue(vol.isEmpty)

        repository!!.deleteTabela(base)
    }

    //@AfterAll
    //fun clear() = repository!!.deleteTabela(base)

}