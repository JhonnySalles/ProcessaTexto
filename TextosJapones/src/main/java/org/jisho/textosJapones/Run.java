package org.jisho.textosJapones;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.jisho.textosJapones.controller.MenuPrincipalController;

public class Run extends Application {

	private static Scene MAIN_SCENE;
	private static Stage PRIMARY_STAGE;

	@Override
	public void start(Stage primaryStage) {
		PRIMARY_STAGE = primaryStage;
		// Menu.runMenu(primaryStage);
		try {
			// Classe inicial
			FXMLLoader loader = new FXMLLoader(MenuPrincipalController.getFxmlLocate());
			AnchorPane scPnTelaPrincipal = loader.load();

			MAIN_SCENE = new Scene(scPnTelaPrincipal); // Carrega a scena
			MAIN_SCENE.setFill(Color.BLACK);
			MAIN_SCENE.getStylesheets().add(Run.class.getResource("/css/Dark_Theme.css").toExternalForm());

			PRIMARY_STAGE.setScene(MAIN_SCENE); // Seta a cena principal
			PRIMARY_STAGE.setTitle("Processar textos japonês");
			PRIMARY_STAGE.getIcons()
					.add(new Image(Run.class.getResourceAsStream(MenuPrincipalController.getIconLocate())));
			PRIMARY_STAGE.initStyle(StageStyle.DECORATED);
			PRIMARY_STAGE.setMinWidth(850);
			PRIMARY_STAGE.setMinHeight(600);
			PRIMARY_STAGE.show(); // Mostra a tela.

		} catch (Exception e) {
			e.printStackTrace();
		}

		PRIMARY_STAGE.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				System.exit(0);
			}
		});
	}

	public static Scene getMainScene() {
		return MAIN_SCENE;
	}

	public static Stage getPrimaryStage() {
		return PRIMARY_STAGE;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
