package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.mock.MockRevisar
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.service.RevisarBaseServices
import br.com.fenix.processatexto.service.VocabularioBaseServices
import javafx.scene.Scene
import javafx.scene.control.TextInputControl
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension::class)
abstract class RevisarControllerTest(conexao: Conexao, var linguagem: Language) {

    init {
        TestsConfig.prepareDatabase()
    }

    private lateinit var scene: Scene
    private lateinit var stage: Stage
    private lateinit var controller: RevisarController

    abstract val vocabulario: VocabularioBaseServices

    @Start
    open fun start(stage: Stage) {
        this.stage = stage
        val loaded = TestsConfig.createScene<RevisarController>(RevisarController.fxmlLocate)

        scene = loaded.first
        controller = loaded.second

        stage.scene = scene
        stage.title = "Testando Revisar"
        stage.minWidth = 900.0
        stage.minHeight = 700.0
        stage.show()

        val robot = FxRobot().targetWindow(scene)
        val language = robot.lookup("#cbLinguagem").queryComboBox<Language>()
        language.selectionModel.select(linguagem)
    }

    protected val entity = MockRevisar(conexao).mockEntity()
    abstract val revisar: RevisarBaseServices

    @BeforeAll
    open fun prepareDatabase(robot: FxRobot) {
        revisar.insert(entity)
    }

    private fun valideRevisao(ingles: TextInputControl, portugues: TextInputControl, isEqual: Boolean = true) {
        if (isEqual) {
            Assertions.assertEquals(ingles.text, entity.ingles)
            Assertions.assertEquals(portugues.text, entity.portugues)
        } else {
            Assertions.assertNotEquals(ingles.text, entity.ingles)
            Assertions.assertNotEquals(portugues.text, entity.portugues)
        }
    }

    @Test
    @Order(1)
    fun carregarRevisao(robot: FxRobot) {
        controller.setLegenda(false)
        controller.setManga(false)
        controller.setNovel(false)

        val novo = robot.lookup("#btnNovo").queryButton()
        robot.clickOn(novo)

        val ingles = robot.lookup("#txtAreaIngles").queryTextInputControl()
        val portugues = robot.lookup("#txtAreaPortugues").queryTextInputControl()

        robot.sleep(50000)

        valideRevisao(ingles, portugues)

        controller.setLegenda(true)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues, isEqual = false)

        controller.setLegenda(false)
        controller.setManga(true)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues, isEqual = false)

        controller.setManga(false)
        controller.setNovel(true)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues, isEqual = false)

        controller.setNovel(false)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues)
    }

    @Test
    @Order(2)
    fun pesquisarRevisao(robot: FxRobot) {
        controller.setLegenda(false)
        controller.setManga(false)
        controller.setNovel(false)

        val novo = robot.lookup("#btnNovo").queryButton()
        val pesquisar = robot.lookup("#txtPesquisar").queryTextInputControl()
        robot.clickOn(pesquisar).write(entity.vocabulario)
        robot.robotContext().keyboardRobot.press(KeyCode.TAB)

        val ingles = robot.lookup("#txtAreaIngles").queryTextInputControl()
        val portugues = robot.lookup("#txtAreaPortugues").queryTextInputControl()

        robot.clickOn(novo)
        valideRevisao(ingles, portugues)

        entity.isManga = true
        revisar.update(entity)
        controller.setManga(false)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues)

        entity.isManga = false
        entity.isNovel = true
        revisar.update(entity)
        controller.setManga(false)
        controller.setNovel(true)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues)

        entity.isNovel = false
        entity.isAnime = true
        revisar.update(entity)
        controller.setNovel(false)
        controller.setLegenda(true)
        robot.clickOn(novo)
        valideRevisao(ingles, portugues)

    }

    @Test
    @Order(3)
    fun formatarRevisao(robot: FxRobot) {
        val portugues = robot.lookup("#txtAreaPortugues").queryTextInputControl()
        robot.clickOn(portugues).write("...")

        Assertions.assertEquals(portugues.text, entity.portugues + "...")

        val formatar = robot.lookup("#btnFormatar").queryButton()
        robot.clickOn(formatar)

        Assertions.assertEquals(portugues.text, entity.portugues)
    }

    @Test
    @Order(4)
    fun testarRevisao(robot: FxRobot) {
        robot.clickOn(robot.lookup("#btnNovo").queryButton())
        val portugues = robot.lookup("#txtAreaPortugues").queryTextInputControl()
        val ingles = robot.lookup("#txtAreaIngles").queryTextInputControl()

        //robot.sleep(1000)

        Assertions.assertTrue(portugues.text.isNotEmpty())

        entity.portugues += " Portugues."
        entity.ingles = "To meet (by chance), to come across, to run across ingles."

        robot.clickOn(portugues).write(" Portugues.")
        robot.clickOn(ingles).write(" Ingles.")

        robot.clickOn(robot.lookup("#btnSalvar").queryButton())
        Assertions.assertTrue(portugues.text.isEmpty())

        val saved = vocabulario.select(entity.vocabulario)
        Assertions.assertTrue(saved.isPresent)
        Assertions.assertEquals(entity.vocabulario, saved.get().vocabulario)
        Assertions.assertEquals(entity.formaBasica, saved.get().formaBasica)
        Assertions.assertEquals(entity.leitura, saved.get().leitura)
        Assertions.assertEquals(entity.leituraNovel, saved.get().leituraNovel)
        Assertions.assertEquals(entity.ingles, saved.get().ingles)
        Assertions.assertEquals(entity.ingles, saved.get().ingles)
    }

    @AfterAll
    open fun clear() {
        if (TestsConfig.LIMPA_LISTA) {
            try {
                revisar.delete(entity)
            } catch (E: Exception) {
            }
            try {
                vocabulario.select(entity.vocabulario).ifPresent { vocabulario.delete(it) }
            } catch (E: Exception) {
            }
        }
    }

}