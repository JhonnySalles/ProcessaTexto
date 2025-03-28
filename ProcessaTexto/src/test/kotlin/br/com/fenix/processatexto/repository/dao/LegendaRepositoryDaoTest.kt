package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.LegendasDao
import br.com.fenix.processatexto.mock.MockFilaSql
import br.com.fenix.processatexto.mock.MockLegenda
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.entities.subtitle.Legenda
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class LegendaRepositoryDaoTest {

    init {
        TestsConfig.prepareDatabase()
    }

    @InjectMocks
    var repository: LegendasDao? = DaoFactory.createLegendasDao()

    private var inputLegenda = MockLegenda()
    private var inputFila = MockFilaSql()

    private lateinit var lastLegenda: Legenda

    private lateinit var lastFila: FilaSQL
    private lateinit var lastList: MutableList<FilaSQL>

    private val TABELA = "tabela_teste"

    @Test
    @Order(1)
    fun testCreateTabela() {
        repository!!.createTabela(TABELA)
    }

    @Test
    @Order(2)
    fun testInserLegenda() {
        lastLegenda = inputLegenda.mockEntity()
        repository!!.insert(TABELA, lastLegenda)
        inputLegenda.assertsService(lastLegenda)
    }

    @Test
    @Order(3)
    fun testUpdateLegenda() {
        repository!!.update(TABELA, lastLegenda)
        inputLegenda.assertsService(lastLegenda)
    }

    @Test
    @Order(4)
    fun testSelectLegenda() {
        val entitie = repository!!.select(TABELA, lastLegenda.getId()!!)
        Assertions.assertTrue(entitie.isPresent)
        inputLegenda.assertsService(lastLegenda, entitie.get())
    }

    @Test
    @Order(5)
    fun testDeleteLegenda() {
        repository!!.delete(TABELA, lastLegenda)
        val entitie = repository!!.select(TABELA, lastLegenda.getId()!!)
        Assertions.assertTrue(entitie.isEmpty)
    }


    @Test
    @Order(6)
    fun testInserFilaSql() {
        lastFila = inputFila.mockEntity()
        repository!!.comandoInsert(lastFila)
        inputFila.assertsService(lastFila)
    }

    @Test
    @Order(7)
    fun testUpdateFilaSql() {
        lastFila = inputFila.updateEntity(lastFila)
        repository!!.comandoUpdate(lastFila)
        inputFila.assertsService(lastFila)
    }

    @Test
    @Order(8)
    fun testSelectFilaSql() {
        val entities = repository!!.comandoSelect()
        Assertions.assertTrue(entities.isNotEmpty())
        inputFila.assertsService(entities[0], lastFila)
    }

    @AfterAll
    fun clear() {
        if (TestsConfig.LIMPA_LISTA) {
            repository!!.comandoDelete("DROP TABLE $TABELA")
            repository!!.comandoDelete("TRUNCATE TABLE _fila_sql")
        }
    }

}