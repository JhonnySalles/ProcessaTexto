package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class LegendasController implements Initializable {

	@FXML
	private AnchorPane apRoot;

	@FXML
	private StackPane stackPane;

	@FXML
	protected AnchorPane apConteinerRoot;

	@FXML
	private LegendasImportarController importarController;

	@FXML
	private LegendasGerarVocabularioController processarController;

	public AnchorPane getRoot() {
		return apConteinerRoot;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		importarController.setControllerPai(this);
		processarController.setControllerPai(this);
	}

	public static URL getFxmlLocate() {
		return LegendasController.class.getResource("/view/Legendas.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
