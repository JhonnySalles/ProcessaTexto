package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.mock.MockFilaSql
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class FilaSqlRepositoryTest : RepositoryTestBase<UUID?, FilaSQL>() {

    @InjectMocks
    override var repository: RepositoryJpa<UUID?, FilaSQL> = object : RepositoryJpaBase<UUID?, FilaSQL>(Conexao.DECKSUBTITLE) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockFilaSql()
    }

}