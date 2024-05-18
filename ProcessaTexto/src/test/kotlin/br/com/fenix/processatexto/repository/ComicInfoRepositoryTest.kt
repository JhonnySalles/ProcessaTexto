package br.com.fenix.processatexto.repository

import br.com.fenix.processatexto.database.jpa.RepositoryJpa
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.mock.MockComicInfo
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class ComicInfoRepositoryTest : RepositoryTestBase<UUID?, ComicInfo>() {

    @InjectMocks
    override var repository: RepositoryJpa<UUID?, ComicInfo> = object : RepositoryJpaBase<UUID?, ComicInfo>(Conexao.PROCESSA_TEXTO) {}

    @BeforeEach
    @Throws(Exception::class)
    override fun setUpMocks() {
        input = MockComicInfo()
    }

}