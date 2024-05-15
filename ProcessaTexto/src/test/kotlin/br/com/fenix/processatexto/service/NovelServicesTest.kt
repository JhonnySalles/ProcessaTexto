package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.repository.VocabularioRepositoryTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class NovelServicesTest : VocabularioRepositoryTest(Conexao.TEXTO_JAPONES) {}