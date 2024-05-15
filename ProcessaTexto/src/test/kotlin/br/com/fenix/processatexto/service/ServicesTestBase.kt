package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.mock.Mock
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
abstract class ServicesTestBase<ID, E> {

    @BeforeAll
    fun configuraConexao() {
        TestsConfig.prepareDatabase()
    }

    protected lateinit var input: Mock<ID, E>

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

    @Test
    @Order(1)
    open fun testeSave() {
        val entity = input.mockEntity(null)
        lastId = save(entity)
        assertNotNull(lastId)
    }

    @Test
    @Order(2)
    open fun testeSelect() {
        input.assertsService(select(lastId!!))
    }

    @Test
    @Order(3)
    open fun testeUpdate() {
        val updated = input.updateEntityById(lastId)
        val saved = update(updated)
        input.assertsService(updated, saved)
    }

    @Test
    @Order(4)
    open fun testeDelete() {
        val saved = select(lastId!!)
        delete(saved!!)
        assertNull(select(lastId!!))
    }

    @Test
    @Order(5)
    open fun testeSaveAll() {
        val list = input.mockEntityList()
        val saved = saveAll(list)
        assertTrue(saved.isNotEmpty())
        input.assertsService(saved[0])
    }

    @Test
    @Order(6)
    open fun testeSelectAll() {
        val list = selectAll()
        assertTrue(list.isNotEmpty())
        input.assertsService(list[0])
    }

    @Test
    @Order(7)
    open fun testeUpdateAll() {
        val list = selectAll().toMutableList()
        for (entity in list)
            input.updateEntity(entity)
        val update = updateAll(list)
        assertTrue(update.isNotEmpty())
        input.assertsService(update[0])
    }

    @Test
    @Order(8)
    open fun testeDeleteAll() {
        val list = selectAll()
        deleteAll(list)
        assertTrue(selectAll().isEmpty())
    }

}