package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Menu;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.model.enums.Tela;

import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class MenuController implements Initializable {

	@FXML
	private AnchorPane apGlobal;
	
	@FXML
	private JFXButton btnVoltar;

	@FXML
	private JFXButton btnCancelar;

	@FXML
	private void onBtnTextoAction() {
		Menu.tela = Tela.TEXTO;
		Run.run();
	}

	@FXML
	private void onBtnLegendaAction() {
		Menu.tela = Tela.LEGENDA;
		Run.run();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public static URL getFxmlLocate() {
		return MenuController.class.getResource("/org/jisho/textosJapones/view/Menu.fxml");
	}
}
