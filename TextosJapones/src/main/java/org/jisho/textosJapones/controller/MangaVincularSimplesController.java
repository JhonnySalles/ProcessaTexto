package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.VinculoPagina;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class MangaVincularSimplesController implements Initializable {

	@FXML
	public HBox hbRoot;
	
	@FXML
	public AnchorPane root;

	@FXML
	public ImageView imagem;

	@FXML
	public Text numero;

	@FXML
	public Text nomePagina;
	
	public void setDados(VinculoPagina vinculo) {
		imagem.setImage(vinculo.getImagemVinculadoEsquerda());
		numero.setText(vinculo.getVinculadoEsquerdaPagina().toString());
		nomePagina.setText(vinculo.getVinculadoEsquerdaNomePagina());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		imagem.fitWidthProperty().bind(root.widthProperty());
		imagem.fitHeightProperty().bind(root.heightProperty());
		imagem.setPreserveRatio(true);
		
	}

	public static URL getFxmlLocate() {
		return MangaVincularSimplesController.class.getResource("/view/MangaVincularSimples.fxml");
	}
}
