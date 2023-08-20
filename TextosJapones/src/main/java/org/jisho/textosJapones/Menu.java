package org.jisho.textosJapones;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jisho.textosJapones.controller.FrasesAnkiController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.enums.Tela;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(Menu.class);

	public static Tela tela = Tela.TEXTO;

	public static void runMenu(Stage primaryStage) {
		try {
			// Classe inicial
			FXMLLoader loader = new FXMLLoader(MenuPrincipalController.getFxmlLocate());
			AnchorPane scPnTelaPrincipal = loader.load();

			Scene scena = new Scene(scPnTelaPrincipal); // Carrega a scena
			scena.setFill(Color.BLACK);

			primaryStage.setScene(scena); // Seta a cena principal
			primaryStage.setTitle("Processar textos japonês");
			primaryStage.getIcons().add(new Image(Menu.class.getResourceAsStream(FrasesAnkiController.getIconLocate())));
			primaryStage.setMinWidth(300);
			primaryStage.setMinHeight(200);

			primaryStage.show(); // Mostra a tela.
		} catch (Exception e) {
			
			LOGGER.error(e.getMessage(), e);
		}
	}
}
