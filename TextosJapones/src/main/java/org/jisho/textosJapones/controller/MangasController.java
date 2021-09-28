package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class MangasController implements Initializable {

	@FXML
	private AnchorPane apRoot;

	@FXML
	private StackPane stackPane;

	@FXML
	protected AnchorPane apConteinerRoot;

	@FXML
	private MangasJsonController jsonController;

	@FXML
	private MangasProcessarController processarController;

	public AnchorPane getRoot() {
		return apConteinerRoot;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		jsonController.setControllerPai(this);
		processarController.setControllerPai(this);
	}

	public static URL getFxmlLocate() {
		return MangasController.class.getResource("/view/Manga.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
