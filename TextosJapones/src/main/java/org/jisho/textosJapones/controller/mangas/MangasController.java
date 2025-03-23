package org.jisho.textosJapones.controller.mangas;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.jisho.textosJapones.model.services.SincronizacaoServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MangasController implements Initializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MangasController.class);

	@FXML
	private AnchorPane apRoot;

	@FXML
	private StackPane stackPane;

	@FXML
	protected AnchorPane apConteinerRoot;

	@FXML
	private Tab tbComicInfo;

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
	
	@FXML
	private MangasComicInfoController comicinfoController;

	public AnchorPane getRoot() {
		return apConteinerRoot;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	@FXML
	private void onSelectComicInfoChanged(Event event) {
		SincronizacaoServices.processarComicInfo = tbComicInfo.isSelected();
		MangasComicInfoController.selecionado = tbComicInfo.isSelected();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		jsonController.setControllerPai(this);
		processarController.setControllerPai(this);
		ajustarController.setControllerPai(this);
		traducaoController.setControllerPai(this);
		vincularController.setControllerPai(this);
		comicinfoController.setControllerPai(this);
	}

	public static URL getFxmlLocate() {
		return MangasController.class.getResource("/view/mangas/Manga.fxml");
	}
}
