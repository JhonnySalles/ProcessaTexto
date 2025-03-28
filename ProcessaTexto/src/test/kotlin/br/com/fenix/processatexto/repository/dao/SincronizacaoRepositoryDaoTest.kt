package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.KanjiDao
import br.com.fenix.processatexto.database.dao.LegendasDao
import br.com.fenix.processatexto.database.dao.SincronizacaoDao
import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.mock.Mock
import br.com.fenix.processatexto.mock.MockFilaSql
import br.com.fenix.processatexto.mock.MockLegenda
import br.com.fenix.processatexto.mock.MockSincronizacao
import br.com.fenix.processatexto.model.entities.processatexto.Processar
import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.entities.subtitle.Legenda
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.sql.SQLException
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class SincronizacaoRepositoryDaoTest {

    init {
        TestsConfig.prepareDatabase()
    }

    @InjectMocks
    var repository: SincronizacaoDao? = DaoFactory.createSincronizacaoDao()

    private var input = MockSincronizacao()

    private lateinit var lastEntity: Sincronizacao

    @Test
    @Order(1)
    fun testInsert() {
        lastEntity = input.mockEntity(Conexao.MANGA_EXTRACTOR)
        repository!!.insert(lastEntity)
        input.assertsService(lastEntity)
    }

    @Test
    @Order(2)
    fun testUpdate() {
        lastEntity = input.updateEntity(lastEntity)
        repository!!.update(lastEntity)
        input.assertsService(lastEntity)
    }

    @Test
    @Order(3)
    fun testSelect() {
        val entity = repository!!.select(lastEntity.conexao)
        Assertions.assertTrue(entity.isPresent)
        input.assertsService(entity.get())
    }

    @Test
    @Order(4)
    fun testDelete() {
        repository!!.delete(lastEntity)
        val entity = repository!!.select(lastEntity.conexao)
        Assertions.assertTrue(entity.isEmpty)
    }

}