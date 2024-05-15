package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.implement.RepositoryJpaImpl
import br.com.fenix.processatexto.mock.MockSincronizacao
import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.mockito.InjectMocks
import java.util.*


abstract class SincronizacaoRepositoryTest(var conexao: Conexao) : RepositoryTestBase<Long, Sincronizacao>() {

    @InjectMocks
    override var repository: RepositoryJpa<Long, Sincronizacao> = object : RepositoryJpaImpl<Long, Sincronizacao>(conexao) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockSincronizacao(conexao)
    }

}