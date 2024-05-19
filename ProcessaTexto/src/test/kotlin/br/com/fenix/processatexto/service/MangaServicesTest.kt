package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.mock.MockManga
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaTabela
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
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

    private val TABELA = "tabela"

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockManga()
    }

    override fun randomId(): UUID? {
        TODO("Not yet implemented")
    }

    override fun selectAll(): List<MangaVolume> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(list: List<MangaVolume>) {
        TODO("Not yet implemented")
    }

    override fun updateAll(list: List<MangaVolume>): List<MangaVolume> {
        TODO("Not yet implemented")
    }

    override fun saveAll(list: List<MangaVolume>): List<MangaVolume> {
        TODO("Not yet implemented")
    }

    override fun delete(entity: MangaVolume) {
        TODO("Not yet implemented")
    }

    override fun select(id: UUID?): MangaVolume? {
        TODO("Not yet implemented")
    }

    override fun update(entity: MangaVolume): MangaVolume {
        TODO("Not yet implemented")
    }

    override fun save(entity: MangaVolume): UUID? {
        TODO("Not yet implemented")
    }

    @Test
    @Order(1)
    override fun testeSave() {

    }

    @Test
    @Order(2)
    override fun testeSelect() {
        val entity = input.mockEntity()
        assertTrue(service.getManga(entity.base, entity.manga, entity.lingua, entity.volume).isPresent)
        val list = service.selectAll(entity.base, entity.manga, entity.volume, entity.capitulo, entity.lingua)
        assertTrue(list.isNotEmpty())
    }


    @Test
    @Order(3)
    override fun testeUpdate() {

    }

    @Test
    @Order(4)
    override fun testeDelete() {

    }

    @Test
    @Order(5)
    override fun testeSaveAll() {

    }

    @Test
    @Order(6)
    override fun testeSelectAll() {

    }

    @Test
    @Order(7)
    override fun testeUpdateAll() {

    }

    @Test
    @Order(8)
    override fun testeDeleteAll() {

    }

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