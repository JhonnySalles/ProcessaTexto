package org.jisho.textosJapones;

import org.jisho.textosJapones.controller.FrasesController;

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

public class Run extends Application {

	private static Scene MAIN_SCENE;
	private static FrasesController MAIN_CONTROLLER;
	private static Stage PRIMARY_STAGE;

	public void start(Stage primaryStage) {
		try {
			PRIMARY_STAGE = primaryStage;
			// Classe inicial
			FXMLLoader loader = new FXMLLoader(FrasesController.getFxmlLocate());
			AnchorPane scPnTelaPrincipal = loader.load();
			MAIN_CONTROLLER = loader.getController();

			MAIN_SCENE = new Scene(scPnTelaPrincipal); // Carrega a scena
			MAIN_SCENE.setFill(Color.BLACK);

			primaryStage.setScene(MAIN_SCENE); // Seta a cena principal
			primaryStage.setTitle("Processar textos japonês");
			primaryStage.getIcons()
					.add(new Image(getClass().getResourceAsStream(FrasesController.getIconLocate())));
			primaryStage.initStyle(StageStyle.DECORATED);
			// primaryStage.setMaximized(true);
			primaryStage.setMinWidth(750);
			primaryStage.setMinHeight(500);

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					System.exit(0);
				}
			});

			primaryStage.show(); // Mostra a tela.

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FrasesController getMainController() {
		return MAIN_CONTROLLER;
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
