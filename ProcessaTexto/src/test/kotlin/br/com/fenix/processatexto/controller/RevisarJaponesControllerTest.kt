package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.mock.MockRevisar
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.service.RevisarBaseServices
import br.com.fenix.processatexto.service.RevisarJaponesServices
import br.com.fenix.processatexto.service.VocabularioBaseServices
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import javafx.scene.Scene
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension::class)
class RevisarJaponesControllerTest : RevisarControllerTest(Conexao.TEXTO_JAPONES, Language.JAPANESE) {

    override val vocabulario: VocabularioBaseServices
        get() = VocabularioJaponesServices()
    override val revisar: RevisarBaseServices
        get() = RevisarJaponesServices()

    @Start
    override fun start(stage: Stage) {
        super.start(stage)
    }

    @BeforeAll
    override fun prepareDatabase(robot: FxRobot) {
        entity.vocabulario = MockRevisar.KANJI
        super.prepareDatabase(robot)
    }

}