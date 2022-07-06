package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class MangaVincularLineController implements Initializable {

	@FXML
	public AnchorPane originalRoot;

	@FXML
	public ImageView originalImagem;

	@FXML
	public Text originalNumero;

	@FXML
	public Text originalNomePagina;

	@FXML
	public AnchorPane esquerdaRoot;

	@FXML
	public ImageView esquerdaImagem;

	@FXML
	public Text esquerdaNumero;

	@FXML
	public Text esquerdaNomePagina;

	@FXML
	public AnchorPane direitaRoot;

	@FXML
	public ImageView direitaImagem;

	@FXML
	public Text direitaNumero;

	@FXML
	public Text direitaNomePagina;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		originalImagem.fitWidthProperty().bind(originalRoot.widthProperty());
		originalImagem.fitHeightProperty().bind(originalRoot.heightProperty());
		originalImagem.setPreserveRatio(true);

		esquerdaImagem.fitWidthProperty().bind(esquerdaRoot.widthProperty());
		esquerdaImagem.fitHeightProperty().bind(esquerdaRoot.heightProperty());
		esquerdaImagem.setPreserveRatio(true);

		direitaImagem.fitWidthProperty().bind(direitaRoot.widthProperty());
		direitaImagem.fitHeightProperty().bind(direitaRoot.heightProperty());
		direitaImagem.setPreserveRatio(true);

	}

	public static URL getFxmlLocate() {
		return MangaVincularLineController.class.getResource("/view/MangaVincularLine.fxml");
	}
}
