package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.mock.MockSincronizacao
import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class SincronizacaoRepositoryTest() : RepositoryTestBase<Conexao, Sincronizacao>() {

    @InjectMocks
    override var repository: RepositoryJpa<Conexao, Sincronizacao> = object : RepositoryJpaBase<Conexao, Sincronizacao>(Conexao.PROCESSA_TEXTO) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockSincronizacao()
    }

    @Test
    @Order(1)
    override fun testCreate() {
        lastId = Conexao.FIREBASE
        lastEntity = input.mockEntity(lastId)
        val persisted = repository.save(lastEntity)
        lastId = persisted.getId()
        Assertions.assertNotNull(lastId)
        input.assertsService(persisted, lastEntity)
    }

}