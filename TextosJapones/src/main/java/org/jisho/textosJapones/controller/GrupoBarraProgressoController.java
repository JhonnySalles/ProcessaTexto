package org.jisho.textosJapones.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class GrupoBarraProgressoController implements Initializable {

	@FXML
	private AnchorPane background;

	@FXML
	private Label lblTitulo;
	
	@FXML
	private Label lblLog;

	@FXML
	private ProgressBar barraProgresso;

	// Sera usado para colorir as barras de progresso
	/*final PseudoClass textoAzul = PseudoClass.getPseudoClass("progress-TextAzul");
	final PseudoClass textoVerde = PseudoClass.getPseudoClass("progress-TextVerde");
	final PseudoClass bProgressAzul = PseudoClass.getPseudoClass("progress-barAzul");
	final PseudoClass bProgressVerde = PseudoClass.getPseudoClass("progress-barVerde");*/

	public AnchorPane getBackground() {
		return background;
	}

	public Label getTitulo() {
		return lblTitulo;
	}

	public Label getLog() {
		return lblLog;
	}

	public ProgressBar getBarraProgresso() {
		return barraProgresso;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	public static URL getFxmlLocate() {
		return MenuPrincipalController.class.getResource("/view/GrupoBarraProgresso.fxml");
	}
}
