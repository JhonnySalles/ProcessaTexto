package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.mock.MockRevisar
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.mockito.InjectMocks
import java.util.*


abstract class RevisarDaoTest(var conexao: Conexao) : TestBaseDao<UUID?, Revisar>() {

    abstract fun createRepository() : RepositoryDao<UUID?, Revisar>

    @InjectMocks
    override var repository: RepositoryDao<UUID?, Revisar> = createRepository()

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockRevisar(conexao)
    }

    @Test
    @Order(10)
    fun testInsertRevisar() {
        lastEntity = input.mockEntity()
        (repository as RevisarDao).insertManual(lastEntity!!)
        lastId = lastEntity!!.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(lastEntity)
        lastList.add(lastEntity!!)
    }

    @Test
    @Order(11)
    fun testUpdateRevisar() {
        lastEntity = input.updateEntity(lastEntity!!)
        (repository as RevisarDao).updateManual(lastEntity!!)
        input.assertsService(lastEntity)
    }

    @Test
    @Order(12)
    fun testSetIsMangNovel() {
        lastEntity = (repository as RevisarDao).select(lastId!!).get()
        lastEntity!!.isNovel = true
        lastEntity!!.isManga = true
        (repository as RevisarDao).setIsNovel(lastEntity!!)
        (repository as RevisarDao).setIsManga(lastEntity!!)
        val entity = (repository as RevisarDao).select(lastEntity!!.getId()!!)
        Assertions.assertTrue(entity.isPresent)
        lastEntity = entity.get()
        lastList.add(lastEntity!!)

        Assertions.assertTrue(lastEntity!!.isNovel)
        Assertions.assertTrue(lastEntity!!.isManga)
    }

    @Test
    @Order(13)
    fun testSelectRevisar() {
        var entity = (repository as RevisarDao).selectRevisar(lastEntity!!.vocabulario, lastEntity!!.isAnime, lastEntity!!.isManga, lastEntity!!.isNovel)
        Assertions.assertTrue(entity.isPresent)

        entity = (repository as RevisarDao).select(lastEntity!!.vocabulario)
        Assertions.assertTrue(entity.isPresent)

        val entities = (repository as RevisarDao).selectAll()
        Assertions.assertTrue(entities.isNotEmpty())
    }


    @Test
    @Order(14)
    open fun testSelectSimilar() {
        lastEntity = input.mockEntity()
        (repository as RevisarDao).insertManual(lastEntity!!)
        val entity = (repository as RevisarDao).selectSimilar(lastEntity!!.vocabulario + "aaaa", lastEntity!!.ingles)
        Assertions.assertTrue(entity.isNotEmpty())
    }

    @Test
    @Order(15)
    open fun testIsValidoExist() {
        val entity = (repository as RevisarDao).isValido(lastEntity!!.vocabulario)
        Assertions.assertTrue(entity.isNotEmpty())
        val exists = (repository as RevisarDao).exist(lastEntity!!.vocabulario)
        Assertions.assertTrue(exists)
    }

    @Test
    @Order(16)
    fun testDelete() {
        (repository as RevisarDao).delete(lastEntity!!)
        val entity = (repository as RevisarDao).select(lastEntity!!.getId()!!)
        Assertions.assertTrue(entity.isEmpty)

        (repository as RevisarDao).insertManual(lastEntity!!)
        (repository as RevisarDao).delete(lastEntity!!.vocabulario)
        val vocabulario = (repository as RevisarDao).select(lastEntity!!.getId()!!)
        Assertions.assertTrue(vocabulario.isEmpty)
    }

}