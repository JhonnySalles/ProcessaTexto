package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.mock.MockRevisar
import br.com.fenix.processatexto.mock.MockVocabulario
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.mockito.InjectMocks
import java.time.LocalDateTime
import java.util.*


abstract class VocabularioRepositoryDaoTest(var conexao: Conexao) : RepositoryTestBaseDao<UUID?, Vocabulario>() {

    abstract fun createRepository() : RepositoryDao<UUID?, Vocabulario>

    @InjectMocks
    override var repository: RepositoryDao<UUID?, Vocabulario> = createRepository()

    private val sincronizacao = LocalDateTime.now()

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockVocabulario(conexao)
    }

    @Test
    @Order(10)
    fun testInsertVocabulario() {
        lastEntity = input.mockEntity()
        (repository as VocabularioDao).insertManual(lastEntity!!)
        lastId = lastEntity!!.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(lastEntity)
        lastList.add(lastEntity!!)
    }

    @Test
    @Order(11)
    fun testUpdateVocabulario() {
        lastEntity = input.updateEntity(lastEntity!!)
        (repository as VocabularioDao).updateManual(lastEntity!!)
        input.assertsService(lastEntity)
    }

    @Test
    @Order(12)
    fun testSelectVocabulario() {
        var entity = (repository as VocabularioDao).select(lastEntity!!.vocabulario, lastEntity!!.formaBasica)
        Assertions.assertTrue(entity.isPresent)

        entity = (repository as VocabularioDao).select(lastEntity!!.vocabulario)
        Assertions.assertTrue(entity.isPresent)

        Assertions.assertTrue((repository as VocabularioDao).exist(lastEntity!!.vocabulario))
    }


    @Test
    @Order(13)
    open fun testExclusao() {
        lastEntity = input.mockEntity()
        (repository as VocabularioDao).insertExclusao(lastEntity!!.vocabulario)

        val entity = (repository as VocabularioDao).selectExclusao()
        Assertions.assertTrue(entity.isNotEmpty())

        Assertions.assertTrue((repository as VocabularioDao).existeExclusao(lastEntity!!.vocabulario, lastEntity!!.formaBasica))
    }

    @Test
    @Order(14)
    open fun testEnvio() {
        val entity = (repository as VocabularioDao).selectEnvioVocabulario(sincronizacao)
        Assertions.assertTrue(entity.isNotEmpty())
        val exclusao = (repository as VocabularioDao).selectExclusaoEnvio(sincronizacao)
        Assertions.assertTrue(exclusao.isNotEmpty())
    }

    @Test
    @Order(15)
    open fun testDelete() {
        (repository as VocabularioDao).insert(lastEntity!!)
        (repository as VocabularioDao).delete(lastEntity!!)
        val entity = (repository as VocabularioDao).select(lastEntity!!.getId()!!)
        Assertions.assertTrue(entity.isEmpty)
    }

}