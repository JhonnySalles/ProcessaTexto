package org.jisho.textosJapones.controller.mangas;

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

	@FXML
	private MangasAjustarController ajustarController;

	@FXML
	private MangasTraducaoController traducaoController;

	@FXML
	private MangasVincularController vincularController;

	public AnchorPane getRoot() {
		return apConteinerRoot;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		jsonController.setControllerPai(this);
		processarController.setControllerPai(this);
		ajustarController.setControllerPai(this);
		traducaoController.setControllerPai(this);
		vincularController.setControllerPai(this);
	}

	public static URL getFxmlLocate() {
		return MangasController.class.getResource("/view/mangas/Manga.fxml");
	}

}
