package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.database.dao.implement.VincularDaoJDBC
import br.com.fenix.processatexto.mock.MockManga
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.enums.Language
import javafx.collections.FXCollections
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class MangaServicesTest : ServicesTestBase<UUID?, MangaVolume>() {

    private val LOGGER = LoggerFactory.getLogger(ServicesTestBase::class.java)

    private val service = MangaServices()

    private val TABELA = "teste"

    @BeforeAll
    @Throws(Exception::class)
    fun praparaTeste() {
        try {
            service.createTabela(TABELA)
        } catch (E : Exception) {
            LOGGER.warn("Não foi possível criar a tabela temporária. Possível existência.", E)
        }
    }

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockManga()
    }

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun select(id: UUID?): MangaVolume? = service.selectVolume(TABELA, id!!).orElse(null)

    override fun save(entity: MangaVolume): MangaVolume = service.saveVolume(TABELA, entity)


    override fun delete(entity: MangaVolume) = service.deleteVolume(TABELA, entity)

    override fun selectAll(): List<MangaVolume> {
        val tabelas = service.selectAll(TABELA, "", 0,0f, MockManga.LINGUAGEM)
        return tabelas[0].volumes
    }

    override fun saveAll(list: List<MangaVolume>): List<MangaVolume> = service.saveVolume(TABELA, list)

    override fun deleteAll(list: List<MangaVolume>) = service.deleteVolume(TABELA, list)

    @Test
    @Order(9)
    fun tabelas() {
        assertTrue(service.tabelas.isNotEmpty())

        val tabela = TABELA + "_criada"
        assertDoesNotThrow { service.createTabela(tabela) }
        assertDoesNotThrow { service.deleteTabela(tabela) }

        assertTrue(service.selectTabelas(true).isNotEmpty())

        val entity = input.mockEntity()
        assertTrue(service.selectTabelas(true, false, TABELA, entity.lingua, entity.manga).isNotEmpty())
        assertTrue(service.selectTabelas(true, false, TABELA, entity.lingua, entity.manga, entity.volume, entity.capitulo).isNotEmpty())
        assertTrue(service.selectTabelasJson(TABELA, entity.manga, entity.volume, entity.capitulo, entity.lingua, true).isNotEmpty())

    }

    @Test
    @Order(10)
    fun dadosTransferir() {
        val entity = input.mockEntity()

        assertDoesNotThrow { service.insertDadosTransferir(TABELA, entity) }

        assertTrue(service.selectDadosTransferir(TABELA, TABELA).isNotEmpty())
        assertTrue(service.getTabelasTransferir(TABELA, TABELA).isNotEmpty())

        assertDoesNotThrow { service.salvarTraducao(TABELA, entity) }
    }

    @Test
    @Order(3)
    fun updateCancel() {
        val entity = input.mockEntity()
        assertDoesNotThrow { service.updateCancel(TABELA, entity) }
    }

    @Test
    @Order(4)
    fun vocabulario() {
        val entity = input.mockEntity()
        assertDoesNotThrow { service.updateVocabularioVolume(TABELA, entity) }
        assertDoesNotThrow { service.updateVocabularioCapitulo(TABELA, entity.capitulos.first()) }
        assertDoesNotThrow { service.updateVocabularioPagina(TABELA, entity.capitulos.first().paginas.first()) }

        assertDoesNotThrow { service.insertVocabularios(TABELA, entity.getId(), null, null, entity.vocabularios) }
        assertDoesNotThrow { service.insertVocabularios(TABELA, null, entity.capitulos.first().getId(), null, entity.capitulos.first().vocabularios) }
        assertDoesNotThrow {
            service.insertVocabularios(
                TABELA,
                null,
                null,
                entity.capitulos.first().paginas.first().getId(),
                entity.capitulos.first().paginas.first().vocabularios
            )
        }
    }

    @Order(5)
    fun salvarAjustes() {
        val entity = input.mockEntity()
        val tabela = FXCollections.observableList(mutableListOf(MangaTabela(entity.base, arrayListOf(entity))))
        assertDoesNotThrow { service.salvarAjustes(tabela) }
    }

    @AfterAll
    override fun clear() {
        if (TestsConfig.LIMPA_LISTA) {
            for (entity in lastList)
                delete(entity)

            service.deleteTabela(TABELA)
        }
    }

}