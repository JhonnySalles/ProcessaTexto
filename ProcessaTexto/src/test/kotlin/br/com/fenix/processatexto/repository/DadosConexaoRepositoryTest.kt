package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.implement.RepositoryJpaImpl
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
    override var repository: RepositoryJpa<Long?, DadosConexao> = object : RepositoryJpaImpl<Long?, DadosConexao>(Conexao.PROCESSA_TEXTO) {}

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
        val entity = input.mockEntity(lastId)
        val persisted = repository.save(entity)
        lastId = persisted.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(persisted, entity)
    }

    @Test
    @Order(2)
    override fun testFindById() {
        val entity = input.mockEntity(lastId)
        val persisted = repository.find(lastId!!).get()
        input.assertsService(persisted, entity)
    }

    @Test
    @Order(3)
    override fun testUpdate() {
        val entity = input.updateEntityById(lastId)
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
        val entities: List<DadosConexao> = input.mockEntityList()
        val persisteds = repository.saveAll(entities)

        Assertions.assertNotNull(persisteds)

        input.assertsService(persisteds[0], entities[0])
        input.assertsService(persisteds[persisteds.size / 2], entities[entities.size / 2])
        input.assertsService(persisteds[persisteds.size - 1], entities[entities.size - 1])
    }

    @Test
    @Order(6)
    override fun testFindAll() {
        val list: List<DadosConexao> = input.mockEntityList()
        val entities = repository.findAll()

        Assertions.assertNotNull(entities)

        input.assertsService(list[0], entities[0])
        input.assertsService(list[list.size / 2], entities[entities.size / 2])
        input.assertsService(list[list.size - 1], entities[entities.size - 1])
    }

    @Test
    @Order(7)
    override fun deleteByEntity() {
        val entity = repository.findAll()[0]
        Assertions.assertNotNull(entity)
        val id = entity.getId()!!
        repository.delete(entity)
        val persisted = repository.find(id)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(8)
    override fun deleteList() {
        val entities = repository.findAll()
        Assertions.assertNotNull(entities)
        for (entity in entities)
            repository.delete(entity)

        val persisteds = repository.findAll()
        Assertions.assertTrue(persisteds.isEmpty())
    }

}