package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.mock.Mock
import br.com.fenix.processatexto.model.entities.EntityBase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
abstract class RepositoryTestBaseDao<ID, E : EntityBase<ID, E>> {

    init {
        TestsConfig.prepareDatabase()
    }

    protected lateinit var input: Mock<ID, E>

    abstract var repository: RepositoryDao<ID, E>

    protected lateinit var lastEntity: E
    protected lateinit var lastList: List<E>

    @BeforeEach
    @Throws(Exception::class)
    abstract fun setUpMocks()

    protected open var lastId: ID? = null


    private fun valideList(oldList: List<E>, newList : List<E>) {
        val listOld = oldList.sortedBy { it.getId()?.toString() ?: "" }
        val listNew = newList.sortedBy { it.getId()?.toString() ?: "" }

        input.assertsService(listOld[0], listNew[0])
        input.assertsService(listOld[listOld.size / 2], listNew[listNew.size / 2])
        input.assertsService(listOld[listOld.size - 1], listNew[listNew.size - 1])
    }

    @Test
    @Order(1)
    open fun testInsert() {
        lastId = null
        lastEntity = input.mockEntity(lastId)
        lastId = repository.insert(lastEntity)
        Assertions.assertNotNull(lastId)
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
        repository.update(entity)
        val persisted = repository.find(lastId!!)
        input.assertsService(entity, persisted.get())
    }

    @Test
    @Order(4)
    open fun testDeleteById() {
        if (!TestsConfig.TESTA_EXCLUIR)
            throw Exception(TestsConfig.EXCLUIR_MENSAGEM)

        repository.delete(lastId!!)
        val persisted = repository.find(lastId!!)
        Assertions.assertTrue(persisted.isEmpty)
    }

    @Test
    @Order(5)
    open fun testFindAll() {
        val entities = repository.findAll()

        Assertions.assertTrue(entities.isNotEmpty())
        valideList(lastList, entities)
    }

    @AfterAll
    open fun clear() {
        if (TestsConfig.LIMPA_LISTA) {
            for (entity in lastList)
                if (entity.getId() != null)
                    repository.delete(entity.getId()!!)
        }
    }

}