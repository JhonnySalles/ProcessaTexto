package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.NovelDao
import br.com.fenix.processatexto.mock.MockNovel
import br.com.fenix.processatexto.model.entities.novelextractor.NovelVolume
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class NovelDaoTest {

    init {
        TestsConfig.prepareDatabase()
    }

    @InjectMocks
    var repository: NovelDao? = DaoFactory.createNovelDao()

    private var input = MockNovel()
    private val base = "teste_novel"

    private lateinit var lastEntity: NovelVolume

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
        input.assertsService(lastEntity)
    }

    @Test
    @Order(3)
    fun testUpdate() {
        //Não implementado
        /*lastEntity = input.updateEntity(lastEntity)

        repository!!.updateVolume(base, lastEntity)
        for (capitulo in lastEntity.capitulos) {
            repository!!.updateCapitulo(base, capitulo)

            for (texto in capitulo.textos)
                repository!!.updateTexto(base, texto)
        }

        if (lastEntity.capa != null)
            repository!!.updateCapa(base, lastEntity.capa!!)

        input.assertsService(lastEntity)*/
    }

    @Test
    @Order(4)
    fun testSelect() {
        var entity = repository!!.selectVolume(base, lastEntity.getId()!!)
        Assertions.assertTrue(entity.isPresent)
        input.assertsService(lastEntity, entity.get())
        lastEntity = entity.get()

        entity = repository!!.selectVolume(base, lastEntity.novel, lastEntity.volume.toInt(), lastEntity.lingua)
        Assertions.assertTrue(entity.isPresent)
        input.assertsService(lastEntity, entity.get())

        val capitulo = lastEntity.capitulos[0]
        val cap = repository!!.selectCapitulo(base, capitulo.id!!)
        Assertions.assertTrue(cap.isPresent)

        var list = repository!!.selectAll(base)
        Assertions.assertTrue(list.isNotEmpty())

        list = repository!!.selectAll(base, lastEntity.novel, lastEntity.volume.toInt(), lastEntity.capitulo, lastEntity.lingua)
        Assertions.assertTrue(list.isNotEmpty())
    }

    @Test
    @Order(5)
    fun testProcessado() {
        repository!!.updateProcessado(base, lastEntity.getId()!!)
        val entity = repository!!.selectVolume(base, lastEntity.getId()!!)
        Assertions.assertTrue(entity.isPresent)
        Assertions.assertFalse(entity.get().isProcessar)
    }

    @Test
    @Order(6)
    fun testDelete() {
        repository!!.deleteVolume(base, lastEntity)

        val vol = repository!!.selectVolume(base, lastEntity.getId()!!)
        Assertions.assertTrue(vol.isEmpty)

        repository!!.deleteTabela(base)
    }

    @AfterAll
    fun clear() = repository!!.deleteTabela(base)

}