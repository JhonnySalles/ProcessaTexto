package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class VocabularioJaponesDaoTest : VocabularioDaoTest(Conexao.TEXTO_JAPONES) {

    override fun createRepository(): RepositoryDao<UUID?, Vocabulario> = DaoFactory.createVocabularioJaponesDao()

}