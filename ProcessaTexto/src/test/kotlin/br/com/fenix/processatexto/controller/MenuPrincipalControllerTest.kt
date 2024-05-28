package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.TestsConfig
import br.com.fenix.processatexto.model.enums.Api
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxAssert
import org.testfx.api.FxRobot
import org.testfx.assertions.api.Assertions.assertThat
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.matcher.control.LabeledMatchers


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension::class)
class MenuPrincipalControllerTest {

    init {
        TestsConfig.prepareDatabase()
    }

    private lateinit var scene: Scene
    private lateinit var stage: Stage
    private lateinit var controller: MenuPrincipalController

    @Start
    private fun start(stage: Stage) {
        this.stage = stage
        val loaded = TestsConfig.createScene<MenuPrincipalController>(MenuPrincipalController.fxmlLocate)

        scene = loaded.first
        controller = loaded.second

        stage.scene = scene
        stage.title = "Testando Processa Textos"
        stage.icons.add(TestsConfig.getIcon(MenuPrincipalController.iconLocate))
        stage.initStyle(StageStyle.DECORATED)
        stage.minWidth = 900.0
        stage.minHeight = 700.0
        stage.show()
    }


    @Test
    fun labelLog() {
        val texto = "Teste de log"
        controller.setLblLog(texto)
        FxAssert.verifyThat("#lblLog", LabeledMatchers.hasText(texto))
        Assertions.assertTrue(controller.getLblLog().text.isNotEmpty())
    }

    @Test
    fun testeBarraProgresso() {
        val progress = controller.criaBarraProgresso()
        Assertions.assertNotNull(progress)
        progress!!.log.text = "Texto de teste"
        progress.titulo.text = "Titulo de teste"
        progress.barraProgresso.progress = 0.5
        Assertions.assertDoesNotThrow { controller.destroiBarraProgresso(progress, "Teste") }
    }

    @Test
    fun testeContaGoogle(robot: FxRobot) {
        val conta = Api.CONTA_PRINCIPAL
        controller.contaGoogle = conta
        Assertions.assertEquals(controller.contaGoogle, conta)
        val combo = robot.lookup("cbContaGoolge").queryComboBox<Api>()
        assertThat(combo).hasExactlyNumItems(Api.values().size)
        assertThatThrownBy { assertThat(combo).hasSelectedItem(Api.CONTA_PRINCIPAL) }
            .isExactlyInstanceOf(AssertionError::class.java)
            .hasMessage("Expected: ComboBox has selection ${Api.CONTA_PRINCIPAL.descricao} but: was ${combo.selectionModel.selectedItem.descricao}")
    }

    @Test
    fun testeCamposSelecao() {
        Assertions.assertDoesNotThrow { controller.modo }
        Assertions.assertNotNull(controller.modo)
        Assertions.assertDoesNotThrow { controller.modo }
        Assertions.assertNotNull(controller.modo)
        Assertions.assertDoesNotThrow { controller.dicionario }
        Assertions.assertNotNull(controller.dicionario)
        Assertions.assertDoesNotThrow { controller.site }
        Assertions.assertNotNull(controller.site)
    }

    @Test
    fun testeAviso() = controller.setAviso("Aviso de teste")

}