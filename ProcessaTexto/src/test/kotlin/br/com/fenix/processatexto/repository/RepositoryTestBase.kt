package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.mock.Mock
import br.com.fenix.processatexto.model.entities.EntityBase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
abstract class RepositoryTestBase<ID, E : EntityBase<ID, E>> {

    init {
        TestsConfig.prepareDatabase()
    }

    protected lateinit var input: Mock<ID, E>

    abstract var repository: RepositoryJpa<ID, E>

    protected lateinit var lastEntity: E
    protected lateinit var lastList: List<E>

    @BeforeEach
    @Throws(Exception::class)
    abstract fun setUpMocks()

    protected open var lastId: ID? = null

    @Test
    @Order(1)
    open fun testCreate() {
        lastId = null
        lastEntity = input.mockEntity(lastId)
        val persisted = repository.save(lastEntity)
        lastId = persisted.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(persisted, lastEntity)
    }

    @Test
    @Order(2)
    open fun testFindById() {
        val persisted = repository.find(lastId!!).get()
        input.assertsService(persisted, lastEntity)
    }

    @Test
    @Order(3)
    open fun testUpdate() {
        val entity = input.updateEntity(lastEntity)
        repository.save(entity)
        val persisted = repository.find(lastId!!)
        input.assertsService(entity, persisted.get())
    }

    @Test
    @Order(4)
    open fun testDeleteById() {
        repository.delete(lastId!!)
        val persisted = repository.find(lastId!!)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(5)
    open fun testSaveAll() {
        lastList = input.mockEntityList()
        val persisteds = repository.saveAll(lastList)

        Assertions.assertTrue(persisteds.isNotEmpty())

        input.assertsService(persisteds[0], lastList[0])
        input.assertsService(persisteds[persisteds.size / 2], lastList[lastList.size / 2])
        input.assertsService(persisteds[persisteds.size - 1], lastList[lastList.size - 1])
    }

    @Test
    @Order(6)
    open fun testFindAll() {
        val entities = repository.findAll()

        Assertions.assertTrue(entities.isNotEmpty())

        input.assertsService(lastList[0], entities[0])
        input.assertsService(lastList[lastList.size / 2], entities[entities.size / 2])
        input.assertsService(lastList[lastList.size - 1], entities[entities.size - 1])
    }

    @Test
    @Order(7)
    open fun testUpdateAll() {
        lastList = input.updateList(lastList)
        repository.saveAll(lastList)
        val persisteds = repository.findAll()

        Assertions.assertTrue(persisteds.isNotEmpty())

        input.assertsService(lastList[0], persisteds[0])
        input.assertsService(lastList[lastList.size / 2], persisteds[persisteds.size / 2])
        input.assertsService(lastList[lastList.size - 1], persisteds[persisteds.size - 1])
    }

    @Test
    @Order(8)
    open fun deleteByEntity() {
        val entity = lastList[0]
        Assertions.assertNotNull(entity)
        val id = entity.getId()!!
        repository.delete(entity)
        val persisted = repository.find(id)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(9)
    open fun deleteList() {
        for (entity in lastList)
            repository.delete(entity)

        val persisteds = repository.findAll()
        Assertions.assertTrue(persisteds.isEmpty())
    }


    @AfterAll
    open fun clear() {
        for (entity in lastList)
            repository.delete(entity)
    }

}