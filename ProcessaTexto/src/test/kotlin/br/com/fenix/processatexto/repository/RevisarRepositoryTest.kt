package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.implement.RepositoryJpaImpl
import br.com.fenix.processatexto.mock.MockRevisar
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.mockito.InjectMocks
import java.util.*


abstract class RevisarRepositoryTest(var conexao: Conexao) : RepositoryTestBase<UUID?, Revisar>() {

    @InjectMocks
    override var repository: RepositoryJpa<UUID?, Revisar> = object : RepositoryJpaImpl<UUID?, Revisar>(conexao) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockRevisar(conexao)
    }

}