package org.jisho.textosJapones;

import java.io.IOException;

import org.jisho.textosJapones.controller.FrasesController;
import org.jisho.textosJapones.controller.LegendasController;
import org.jisho.textosJapones.controller.MangasController;

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
	private static Stage PRIMARY_STAGE;

	@Override
	public void start(Stage primaryStage) {
		PRIMARY_STAGE = primaryStage;
		Menu.runMenu(primaryStage);
	}

	public static void run() {
		PRIMARY_STAGE.close();
		PRIMARY_STAGE = new Stage();

		switch (Menu.tela) {
		case TEXTO:
			runTexto(PRIMARY_STAGE);
			break;
		case LEGENDA:
			runLegenda(PRIMARY_STAGE);
			break;
		case MANGA:
			runManga(PRIMARY_STAGE);
			break;
		default:
			runTexto(PRIMARY_STAGE);
		}

		PRIMARY_STAGE.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				System.exit(0);
			}
		});
	}

	public static void runTexto(Stage stage) {
		try {
			// Classe inicial
			FXMLLoader loader = new FXMLLoader(FrasesController.getFxmlLocate());
			AnchorPane scPnTelaPrincipal = loader.load();

			MAIN_SCENE = new Scene(scPnTelaPrincipal); // Carrega a scena
			MAIN_SCENE.setFill(Color.BLACK);

			stage.setScene(MAIN_SCENE); // Seta a cena principal
			stage.setTitle("Processar textos japonês");
			stage.getIcons().add(new Image(Run.class.getResourceAsStream(FrasesController.getIconLocate())));
			stage.initStyle(StageStyle.DECORATED);
			stage.setMinWidth(750);
			stage.setMinHeight(500);
			stage.show(); // Mostra a tela.

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void runLegenda(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LegendasController.getFxmlLocate());
			AnchorPane newAnchorPane = loader.load();

			Scene mainScene = new Scene(newAnchorPane); // Carrega a scena
			mainScene.setFill(Color.BLACK);

			stage.setScene(mainScene); // Seta a cena principal
			stage.setTitle("Legendas e correção não encontrados");
			stage.initStyle(StageStyle.DECORATED);
			stage.getIcons().add(new Image(Run.class.getResourceAsStream(LegendasController.getIconLocate())));
			stage.show(); // Mostra a tela.

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runManga(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MangasController.getFxmlLocate());
			AnchorPane newAnchorPane = loader.load();

			Scene mainScene = new Scene(newAnchorPane); // Carrega a scena
			mainScene.setFill(Color.BLACK);

			stage.setScene(mainScene); // Seta a cena principal
			stage.setTitle("Vocabulário de mangas");
			stage.initStyle(StageStyle.DECORATED);
			stage.getIcons().add(new Image(Run.class.getResourceAsStream(MangasController.getIconLocate())));
			stage.show(); // Mostra a tela.

		} catch (IOException e) {
			e.printStackTrace();
		}
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
