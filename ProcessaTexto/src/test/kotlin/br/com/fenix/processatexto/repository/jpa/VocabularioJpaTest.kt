package br.com.fenix.processatexto.repository.jpa

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.mock.MockVocabulario
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.mockito.InjectMocks
import java.util.*


abstract class VocabularioJpaTest(var conexao: Conexao) : TestBaseJpa<UUID?, Vocabulario>() {

    @InjectMocks
    override var repository: RepositoryJpa<UUID?, Vocabulario> = object : RepositoryJpaBase<UUID?, Vocabulario>(conexao) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockVocabulario(conexao)
    }

}