package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.mock.MockManga
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.enums.Language
import javafx.collections.FXCollections
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class MangaServicesTest : ServicesTestBase<UUID?, MangaVolume>() {

    private val service = MangaServices()

    private val TABELA = "teste"

    @BeforeAll
    @Throws(Exception::class)
    fun praparaTeste() {
        service.createTabela(TABELA)
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
        val entity = input.mockEntity()

        assertTrue(service.tabelas.isNotEmpty())

        assertDoesNotThrow { service.createTabela(entity.base) }
        assertTrue(service.selectTabelas(true).isNotEmpty())

        assertTrue(service.selectTabelas(true, false, entity.base, entity.lingua, entity.manga).isNotEmpty())
        assertTrue(service.selectTabelas(true, false, entity.base, entity.lingua, entity.manga, entity.volume, entity.capitulo).isNotEmpty())
        assertTrue(service.selectTabelasJson(entity.base, entity.manga, entity.volume, entity.capitulo, entity.lingua, true).isNotEmpty())
    }

    @Test
    @Order(10)
    fun dadosTransferir() {
        val entity = input.mockEntity()

        assertDoesNotThrow { service.insertDadosTransferir(entity.base, entity) }

        assertTrue(service.selectDadosTransferir(entity.base, TABELA).isNotEmpty())
        assertTrue(service.getTabelasTransferir(entity.base, TABELA).isNotEmpty())

        assertDoesNotThrow { service.salvarTraducao(entity.base, entity) }
    }

    @Test
    @Order(3)
    fun updateCancel() {
        val entity = input.mockEntity()
        assertDoesNotThrow { service.updateCancel(entity.base, entity) }
    }

    @Test
    @Order(4)
    fun vocabulario() {
        val entity = input.mockEntity()
        assertDoesNotThrow { service.updateVocabularioVolume(entity.base, entity) }
        assertDoesNotThrow { service.updateVocabularioCapitulo(entity.base, entity.capitulos.first()) }
        assertDoesNotThrow { service.updateVocabularioPagina(entity.base, entity.capitulos.first().paginas.first()) }

        assertDoesNotThrow { service.insertVocabularios(entity.base, entity.getId(), null, null, entity.vocabularios) }
        assertDoesNotThrow { service.insertVocabularios(entity.base, null, entity.capitulos.first().getId(), null, entity.capitulos.first().vocabularios) }
        assertDoesNotThrow {
            service.insertVocabularios(
                entity.base,
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

}