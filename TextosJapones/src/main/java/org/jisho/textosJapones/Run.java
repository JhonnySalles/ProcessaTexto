package org.jisho.textosJapones;

import org.jisho.textosJapones.controller.ProcessarFrasesController;

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

	private static Scene mainScene;
	private static ProcessarFrasesController mainController;

	public void start(Stage primaryStage) {
		try {
			// Classe inicial
			FXMLLoader loader = new FXMLLoader(ProcessarFrasesController.getFxmlLocate());
			AnchorPane scPnTelaPrincipal = loader.load();
			mainController = loader.getController();

			mainScene = new Scene(scPnTelaPrincipal); // Carrega a scena
			mainScene.setFill(Color.BLACK);

			primaryStage.setScene(mainScene); // Seta a cena principal
			primaryStage.setTitle("Processar textos japonês");
			primaryStage.getIcons()
					.add(new Image(getClass().getResourceAsStream("resources/images/icoTextoJapones_128.png")));
			primaryStage.initStyle(StageStyle.DECORATED);
			// primaryStage.setMaximized(true);
			primaryStage.setMinWidth(500);
			primaryStage.setMinHeight(400);

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

	public static ProcessarFrasesController getMainController() {
		return mainController;
	}

	public static Scene getMainScene() {
		return mainScene;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
