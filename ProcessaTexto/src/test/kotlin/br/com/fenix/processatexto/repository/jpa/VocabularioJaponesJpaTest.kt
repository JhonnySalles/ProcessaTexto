package br.com.fenix.processatexto.repository.jpa

import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class VocabularioJaponesJpaTest : VocabularioJpaTest(Conexao.TEXTO_JAPONES) {}