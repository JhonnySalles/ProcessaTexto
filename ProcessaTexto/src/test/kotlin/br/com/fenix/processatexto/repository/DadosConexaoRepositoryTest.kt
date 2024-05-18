package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.mock.MockDadosConexao
import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class DadosConexaoRepositoryTest : RepositoryTestBase<Long?, DadosConexao>() {

    @InjectMocks
    override var repository: RepositoryJpa<Long?, DadosConexao> = object : RepositoryJpaBase<Long?, DadosConexao>(Conexao.PROCESSA_TEXTO) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockDadosConexao()
    }

    override var lastId: Long? = null

    @Test
    @Order(1)
    override fun testCreate() {
        lastId = null
        lastEntity = input.mockEntity(lastId)
        val persisted = repository.save(lastEntity)
        lastId = persisted.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(persisted, lastEntity)
    }

    @Test
    @Order(2)
    override fun testFindById() {
        val persisted = repository.find(lastId!!).get()
        input.assertsService(persisted, lastEntity)
    }

    @Test
    @Order(3)
    override fun testUpdate() {
        val entity = input.updateEntity(lastEntity)
        repository.save(entity)
        val persisted = repository.find(lastId!!)
        input.assertsService(entity, persisted.get())
    }

    @Test
    @Order(4)
    override fun testDeleteById() {
        repository.delete(lastId!!)
        val persisted = repository.find(lastId!!)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(5)
    override fun testSaveAll() {
        lastList = input.mockEntityList()
        val persisteds = repository.saveAll(lastList)

        Assertions.assertTrue(persisteds.isNotEmpty())

        input.assertsService(persisteds[persisteds.size - 2], lastList[lastList.size - 2])
        input.assertsService(persisteds[persisteds.size - 1], lastList[lastList.size - 1])
    }

    @Test
    @Order(6)
    override fun testFindAll() {
        val entities = repository.findAll()

        Assertions.assertTrue(entities.isNotEmpty())

        input.assertsService(entities[entities.size - 2], lastList[lastList.size - 2])
        input.assertsService(entities[entities.size - 1], lastList[lastList.size - 1])
    }

    @Test
    @Order(7)
    override fun testUpdateAll() {
        lastList = input.updateList(lastList)
        repository.saveAll(lastList)
        val persisteds = repository.findAll()

        Assertions.assertTrue(persisteds.isNotEmpty())

        input.assertsService(persisteds[persisteds.size - 2], lastList[lastList.size - 2])
        input.assertsService(persisteds[persisteds.size - 1], lastList[lastList.size - 1])
    }

    @Test
    @Order(8)
    override fun deleteByEntity() {
        val entity = lastList[0]
        Assertions.assertNotNull(entity)
        val id = entity.getId()!!
        repository.delete(entity)
        val persisted = repository.find(id)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(9)
    override fun deleteList() {
        for (entity in lastList)
            repository.delete(entity)

        val persisteds = repository.findAll()
        for (entity in lastList)
            Assertions.assertFalse(persisteds.contains(entity))
    }

    @AfterAll
    override fun clear() {

    }

}