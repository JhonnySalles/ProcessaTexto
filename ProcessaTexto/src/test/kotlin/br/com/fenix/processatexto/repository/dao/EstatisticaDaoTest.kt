package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.EstatisticaDao
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.mock.MockEstatistica
import br.com.fenix.processatexto.model.entities.processatexto.japones.Estatistica
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class EstatisticaDaoTest : TestBaseDao<UUID?, Estatistica>() {

    @InjectMocks
    override var repository: RepositoryDao<UUID?, Estatistica> = DaoFactory.createEstatisticaDao()

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockEstatistica()
    }

    @Test
    @Order(10)
    fun testSave() {
        lastEntity = input.mockEntity()
        lastEntity!!.setId(null)
        (repository as EstatisticaDao).save(lastEntity!!)
        lastId = lastEntity!!.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(lastEntity)

        lastEntity = input.updateEntity(lastEntity!!)
        (repository as EstatisticaDao).save(lastEntity!!)
        input.assertsService(lastEntity)
        lastList.add(lastEntity!!)
    }

    @Test
    @Order(11)
    fun testSelectKanjiLeitura() {
        val entitie = (repository as EstatisticaDao).select(lastEntity!!.kanji, lastEntity!!.leitura)
        Assertions.assertTrue(entitie.isPresent)
        lastEntity = entitie.get()
        input.assertsService(lastEntity)
    }

    @Test
    @Order(12)
    fun testSelectKanji() {
        val entitie = (repository as EstatisticaDao).select(lastEntity!!.kanji)
        Assertions.assertTrue(entitie.isNotEmpty())
    }

    @Test
    @Order(13)
    fun testSelectAll() {
        val entitie = (repository as EstatisticaDao).selectAll()
        Assertions.assertTrue(entitie.isNotEmpty())
    }

    @Test
    @Order(14)
    fun testDelete() {
        (repository as EstatisticaDao).delete(lastEntity!!)
    }

}