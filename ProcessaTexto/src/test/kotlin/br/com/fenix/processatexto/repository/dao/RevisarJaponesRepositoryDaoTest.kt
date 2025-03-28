package br.com.fenix.processatexto.repository.dao

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class RevisarJaponesRepositoryDaoTest : RevisarRepositoryDaoTest(Conexao.TEXTO_JAPONES) {

    override fun createRepository(): RepositoryDao<UUID?, Revisar> = DaoFactory.createRevisarJaponesDao()

    @Test
    @Order(15)
    override fun testIsValidoExist() {
        val exists = (repository as RevisarDao).exist(lastEntity!!.vocabulario)
        Assertions.assertTrue(exists)
    }

}