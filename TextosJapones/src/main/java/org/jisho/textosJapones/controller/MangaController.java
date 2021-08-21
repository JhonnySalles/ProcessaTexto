package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Site;

import com.jfoenix.controls.JFXComboBox;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class MangaController implements Initializable {

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

	@FXML
	private JFXComboBox<Api> cbContaGoolge;

	@FXML
	private JFXComboBox<Site> cbSite;

	@FXML
	private JFXComboBox<Modo> cbModo;

	@FXML
	private JFXComboBox<Dicionario> cbDicionario;

	@FXML
	private Label lblLog;

	@FXML
	private ProgressBar barraProgresso;

	public Api getContaGoogle() {
		return cbContaGoolge.getSelectionModel().getSelectedItem();
	}

	public Site getSiteTraducao() {
		return cbSite.getSelectionModel().getSelectedItem();
	}

	public Modo getModo() {
		return cbModo.getSelectionModel().getSelectedItem();
	}

	public Dicionario getDicionario() {
		return cbDicionario.getSelectionModel().getSelectedItem();
	}

	public AnchorPane getRoot() {
		return root;
	}

	public StackPane getStackPane() {
		return rootStackPane;
	}

	public ProgressBar getBarraProgresso() {
		return barraProgresso;
	}

	public Label getLog() {
		return lblLog;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		cbContaGoolge.getItems().addAll(Api.values());
		cbContaGoolge.getSelectionModel().selectFirst();

		cbSite.getItems().addAll(Site.values());
		cbSite.getSelectionModel().selectFirst();

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

		cbDicionario.getItems().addAll(Dicionario.values());
		cbDicionario.getSelectionModel().select(Dicionario.FULL);
	}

	public static URL getFxmlLocate() {
		return MangaController.class.getResource("/view/Manga.fxml");
	}

}
