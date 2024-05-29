package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.mock.MockRevisar
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.service.*
import javafx.stage.Stage
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension::class)
class RevisarInglesControllerTest : RevisarControllerTest(Conexao.TEXTO_INGLES, Language.ENGLISH) {

    override val vocabulario: VocabularioBaseServices
        get() = VocabularioInglesServices()
    override val revisar: RevisarBaseServices
        get() = RevisarInglesServices()

    @Start
    override fun start(stage: Stage) {
        super.start(stage)
    }

    @BeforeAll
    override fun prepareDatabase(robot: FxRobot) {
        entity.vocabulario = MockRevisar.WORD
        super.prepareDatabase(robot)
    }

}