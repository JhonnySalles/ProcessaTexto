package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class RevisarInglesRepositoryDaoTest : RevisarRepositoryDaoTest(Conexao.TEXTO_INGLES) {

    override fun createRepository(): RepositoryDao<UUID?, Revisar> = DaoFactory.createRevisarInglesDao()

    @Test
    @Order(1)
    override fun testInsert() {
        lastId = null
        lastEntity = input.mockEntity(lastId)
        (repository as RevisarDao).insertManual(lastEntity!!)
        lastId = lastEntity!!.getId()
        Assertions.assertNotNull(lastId)
    }

    @Test
    @Order(2)
    override fun testFindById() {
        val persisted = (repository as RevisarDao).select(lastId!!).get()
        input.assertsService(persisted, lastEntity)
    }

    @Test
    @Order(3)
    override fun testUpdate() {
        lastEntity = input.updateEntity(lastEntity!!)
        (repository as RevisarDao).updateManual(lastEntity!!)
        val persisted = (repository as RevisarDao).select(lastId!!)
        input.assertsService(lastEntity, persisted.get())
    }

    @Test
    @Order(4)
    override fun testFindAll() {
        lastList = mutableListOf(lastEntity!!)
        val entities = (repository as RevisarDao).selectAll()
        Assertions.assertTrue(entities.isNotEmpty())
        valideList(lastList, entities)
    }


    @Test
    @Order(5)
    override fun testDeleteById() {
        if (!TestsConfig.TESTA_EXCLUIR)
            throw Exception(TestsConfig.EXCLUIR_MENSAGEM)

        (repository as RevisarDao).delete(lastId!!)
        val persisted = (repository as RevisarDao).select(lastId!!)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(14)
    override fun testSelectSimilar() {
        //Ingles não possui similar
    }

    @Test
    @Order(15)
    override fun testIsValidoExist() {
        val exists = (repository as RevisarDao).exist(lastEntity!!.vocabulario)
        Assertions.assertTrue(exists)
    }

}