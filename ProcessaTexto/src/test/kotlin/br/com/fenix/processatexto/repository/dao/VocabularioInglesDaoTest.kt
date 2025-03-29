package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class VocabularioInglesDaoTest : VocabularioDaoTest(Conexao.TEXTO_INGLES) {

    override fun createRepository(): RepositoryDao<UUID?, Vocabulario> = DaoFactory.createVocabularioInglesDao()

    @Test
    @Order(1)
    override fun testInsert() {
        lastId = null
        lastEntity = input.mockEntity(lastId)
        (repository as VocabularioDao).insertManual(lastEntity!!)
        lastId = lastEntity!!.getId()
        Assertions.assertNotNull(lastId)
    }

    @Test
    @Order(2)
    override fun testFindById() {
        val persisted = (repository as VocabularioDao).select(lastId!!).get()
        input.assertsService(persisted, lastEntity)
    }

    @Test
    @Order(3)
    override fun testUpdate() {
        lastEntity = input.updateEntity(lastEntity!!)
        (repository as VocabularioDao).updateManual(lastEntity!!)
        val persisted = (repository as VocabularioDao).select(lastId!!)
        input.assertsService(lastEntity, persisted.get())
    }

    @Test
    @Order(4)
    override fun testFindAll() {
        // Não implementado
    }


    @Test
    @Order(5)
    override fun testDeleteById() {
        if (!TestsConfig.TESTA_EXCLUIR)
            throw Exception(TestsConfig.EXCLUIR_MENSAGEM)

        (repository as VocabularioDao).delete(lastId!!)
        val persisted = (repository as VocabularioDao).select(lastId!!)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(15)
    override fun testDelete() {
        // Não possui função de delete
    }

}