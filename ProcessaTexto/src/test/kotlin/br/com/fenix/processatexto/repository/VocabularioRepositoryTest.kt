package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.implement.RepositoryJpaImpl
import br.com.fenix.processatexto.mock.MockVocabulario
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.mockito.InjectMocks
import java.util.*


abstract class VocabularioRepositoryTest(var conexao: Conexao) : RepositoryTestBase<UUID?, Vocabulario>() {

    @InjectMocks
    override var repository: RepositoryJpa<UUID?, Vocabulario> = object : RepositoryJpaImpl<UUID?, Vocabulario>(conexao) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockVocabulario(conexao)
    }

}