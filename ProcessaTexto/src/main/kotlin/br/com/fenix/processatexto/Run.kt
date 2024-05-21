package br.com.fenix.processatexto

import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.database.JdbcFactory
import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess


class Run : Application() {

    private val LOGGER = LoggerFactory.getLogger(Application::class.java)

    override fun start(primaryStage: Stage) {
        PRIMARY_STAGE = primaryStage
        // Menu.runMenu(primaryStage);
        try {
            // Classe inicial
            val loader = FXMLLoader(MenuPrincipalController.fxmlLocate)
            val scPnTelaPrincipal = loader.load<AnchorPane>()
            MAIN_SCENE = Scene(scPnTelaPrincipal) // Carrega a scena
            MAIN_SCENE.fill = Color.BLACK
            MAIN_SCENE.stylesheets.add(Run::class.java.getResource("/css/Dark_Theme.css")!!.toExternalForm())
            PRIMARY_STAGE.scene = MAIN_SCENE // Seta a cena principal
            PRIMARY_STAGE.title = "Processar Textos"
            PRIMARY_STAGE.icons.add(Image(Run::class.java.getResourceAsStream(MenuPrincipalController.iconLocate)))
            PRIMARY_STAGE.initStyle(StageStyle.DECORATED)
            PRIMARY_STAGE.minWidth = 900.0
            PRIMARY_STAGE.minHeight = 700.0
            PRIMARY_STAGE.show() // Mostra a tela.
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
        PRIMARY_STAGE.onCloseRequest = EventHandler { exitProcess(0) }
    }

    fun main(args: Array<String>) {
        launch(*args)
    }

    override fun stop() {
        JdbcFactory.closeConnection()
    }

    companion object {
        private lateinit var MAIN_SCENE: Scene
        private lateinit var PRIMARY_STAGE: Stage

        fun getMainScene(): Scene {
            return MAIN_SCENE
        }

        fun getPrimaryStage(): Stage {
            return PRIMARY_STAGE
        }
    }

}