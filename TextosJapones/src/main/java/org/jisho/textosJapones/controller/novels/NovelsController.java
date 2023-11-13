package org.jisho.textosJapones.controller.novels;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.jisho.textosJapones.controller.mangas.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class NovelsController implements Initializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(NovelsController.class);

	@FXML
	private AnchorPane apRoot;

	@FXML
	private StackPane stackPane;

	@FXML
	protected AnchorPane apConteinerRoot;

	@FXML
	private NovelsProcessarController processarController;


	public AnchorPane getRoot() {
		return apConteinerRoot;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		processarController.setControllerPai(this);
	}

	public static URL getFxmlLocate() {
		return NovelsController.class.getResource("/view/novels/Novel.fxml");
	}

}
