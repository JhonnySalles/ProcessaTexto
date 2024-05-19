package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.mock.Mock
import br.com.fenix.processatexto.model.entities.Entity
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
abstract class ServicesTestBase<ID, E : Entity<ID, E>> {

    init {
        TestsConfig.prepareDatabase()
    }

    protected lateinit var input: Mock<ID, E>

    protected lateinit var lastEntity: E
    protected lateinit var lastList: List<E>

    @BeforeEach
    @Throws(Exception::class)
    abstract fun setUpMocks()

    protected open var lastId: ID? = null

    abstract fun randomId(): ID

    abstract fun save(entity: E): ID?
    abstract fun update(entity: E): E
    abstract fun select(id: ID): E?
    abstract fun delete(entity: E)

    abstract fun saveAll(list: List<E>): List<E>
    abstract fun updateAll(list: List<E>): List<E>
    abstract fun selectAll(): List<E>
    abstract fun deleteAll(list: List<E>)

    private fun valideList(oldList: List<E>, newList: List<E>) {
        val listOld = oldList.sortedBy { it.getId()?.toString() ?: "" }
        val listNew = newList.sortedBy { it.getId()?.toString() ?: "" }

        input.assertsService(listOld[0], listNew[0])
        input.assertsService(listOld[listOld.size / 2], listNew[listNew.size / 2])
        input.assertsService(listOld[listOld.size - 1], listNew[listNew.size - 1])
    }

    @Test
    @Order(1)
    open fun testeSave() {
        lastEntity = input.mockEntity(null)
        lastId = save(lastEntity)
        assertNotNull(lastId)
    }

    @Test
    @Order(2)
    open fun testeSelect() {
        input.assertsService(lastEntity, select(lastId!!))
    }

    @Test
    @Order(3)
    open fun testeUpdate() {
        val entity = input.updateEntity(lastEntity)
        val saved = update(entity)
        input.assertsService(entity, saved)
    }

    @Test
    @Order(4)
    open fun testeDelete() {
        if (!TestsConfig.TESTA_EXCLUIR)
            throw Exception(TestsConfig.EXCLUIR_MENSAGEM)

        delete(lastEntity)
        assertNull(select(lastId!!))
    }

    @Test
    @Order(5)
    open fun testeSaveAll() {
        lastList = input.mockEntityList()
        val saved = saveAll(lastList)
        assertTrue(saved.isNotEmpty())
        valideList(lastList, saved)
    }

    @Test
    @Order(6)
    open fun testeSelectAll() {
        val list = selectAll()
        assertTrue(list.isNotEmpty())
        valideList(lastList, list)
    }

    @Test
    @Order(7)
    open fun testeUpdateAll() {
        lastList = input.updateList(lastList)
        val updated = updateAll(lastList)
        assertTrue(updated.isNotEmpty())
        valideList(lastList, updated)
    }

    @Test
    @Order(8)
    open fun testeDeleteAll() {
        if (!TestsConfig.TESTA_EXCLUIR)
            throw Exception(TestsConfig.EXCLUIR_MENSAGEM)

        deleteAll(lastList)
        assertTrue(selectAll().isEmpty())
    }

    @AfterAll
    open fun clear() {
        if (TestsConfig.LIMPA_LISTA)
            for (entity in lastList)
                delete(entity)
    }

}