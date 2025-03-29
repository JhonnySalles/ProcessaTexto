package br.com.fenix.processatexto.repository.jpa

import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class RevisarInglesJpaTest : RevisarJpaTest(Conexao.TEXTO_JAPONES) {

    @Test
    @Order(1)
    override fun testCreate() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(2)
    override fun testFindById() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(3)
    override fun testUpdate() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(4)
    override fun testDeleteById() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(5)
    override fun testSaveAll() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(6)
    override fun testFindAll() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(7)
    override fun testUpdateAll() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(8)
    override fun deleteByEntity() {
        // Modelo difere do padrão japones
    }

    @Test
    @Order(9)
    override fun deleteList() {
        // Modelo difere do padrão japones
    }
    
}