package br.com.fenix.processatexto

import br.com.fenix.processatexto.controller.FrasesAnkiController
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.enums.Tela
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object Menu {
    private val LOGGER: Logger = LoggerFactory.getLogger(Menu::class.java)
    var tela: Tela = Tela.TEXTO

    fun runMenu(primaryStage: Stage) {
        try {
            // Classe inicial
            val loader = FXMLLoader(MenuPrincipalController.fxmlLocate)
            val scPnTelaPrincipal: AnchorPane = loader.load()
            val scena = Scene(scPnTelaPrincipal) // Carrega a scena
            scena.fill = Color.BLACK
            primaryStage.scene = scena // Seta a cena principal
            primaryStage.title = "Processar textos japonês"
            primaryStage.icons.add(Image(Menu::class.java.getResourceAsStream(FrasesAnkiController.iconLocate)))
            primaryStage.minWidth = 300.0
            primaryStage.minHeight = 200.0
            primaryStage.show() // Mostra a tela.
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
    }
}