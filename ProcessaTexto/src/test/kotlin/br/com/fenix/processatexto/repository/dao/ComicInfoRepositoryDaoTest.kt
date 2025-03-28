package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.ComicInfoDao
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.mock.MockComicInfo
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class ComicInfoRepositoryDaoTest : RepositoryTestBaseDao<UUID?, ComicInfo>() {

    @InjectMocks
    override var repository: RepositoryDao<UUID?, ComicInfo> = DaoFactory.createComicInfoDao()

    private val inicio = LocalDateTime.now()

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockComicInfo()
    }

    @Test
    @Order(10)
    fun testSave() {
        lastEntity = input.mockEntity()
        lastEntity!!.setId(null)
        (repository as ComicInfoDao).save(lastEntity!!)
        lastId = lastEntity!!.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(lastEntity)

        lastEntity = input.updateEntity(lastEntity!!)
        (repository as ComicInfoDao).save(lastEntity!!)
        input.assertsService(lastEntity)
        lastList.add(lastEntity!!)
    }


    @Test
    @Order(11)
    fun testFindByIdOrComicOrLanguage() {
        val persisted = (repository as ComicInfoDao).find(lastId!!, lastEntity!!.comic, lastEntity!!.languageISO).get()
        input.assertsService(persisted, lastEntity)
    }


    @Test
    @Order(12)
    fun testFindEnvio() {
        val entities = (repository as ComicInfoDao).findEnvio(inicio)
        Assertions.assertTrue(entities.isNotEmpty())
    }

}