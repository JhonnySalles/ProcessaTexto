package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.mock.MockNovel
import br.com.fenix.processatexto.model.entities.novelextractor.NovelVolume
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class NovelServicesTest : ServicesTestBase<UUID?, NovelVolume>() {

    private val LOGGER = LoggerFactory.getLogger(ServicesTestBase::class.java)

    private val service = NovelServices()

    private val TABELA = "teste"

    @BeforeAll
    @Throws(Exception::class)
    fun praparaTeste() {
        try {
            service.createTabela(TABELA)
        } catch (E: Exception) {
            LOGGER.warn("Não foi possível criar a tabela temporária. Possível existência.", E)
        }
    }

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockNovel()
    }

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun select(id: UUID?): NovelVolume? = service.selectVolume(TABELA, id!!).orElse(null)

    override fun save(entity: NovelVolume): NovelVolume = service.saveVolume(TABELA, entity)


    override fun delete(entity: NovelVolume) = service.deleteVolume(TABELA, entity)

    override fun selectAll(): List<NovelVolume> = service.selectAll(TABELA, "", 0, MockNovel.LINGUAGEM)

    override fun saveAll(list: List<NovelVolume>): List<NovelVolume> = service.saveVolume(TABELA, list)

    override fun deleteAll(list: List<NovelVolume>) = service.deleteVolume(TABELA, list)


    @AfterAll
    override fun clear() {
        if (TestsConfig.LIMPA_LISTA) {
            for (entity in lastList)
                delete(entity)

            service.deleteTabela(TABELA)
        }
    }

}