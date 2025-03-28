package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.EstatisticaDao
import br.com.fenix.processatexto.database.dao.KanjiDao
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.mock.MockKanji
import br.com.fenix.processatexto.model.entities.processatexto.Kanji
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
class KanjiRepositoryDaoTest : RepositoryTestBaseDao<UUID?, Kanji>() {

    @InjectMocks
    override var repository: RepositoryDao<UUID?, Kanji> = DaoFactory.createKanjiDao()

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockKanji()
    }

    @Test
    @Order(10)
    fun testSelectKanji() {
        lastEntity = input.mockEntity()
        repository.insert(lastEntity!!)
        lastList.add(lastEntity!!)
        val entitie = (repository as KanjiDao).select(lastEntity!!.kanji)
        Assertions.assertTrue(entitie.isPresent)
        lastEntity = entitie.get()
        input.assertsService(lastEntity)
    }

}