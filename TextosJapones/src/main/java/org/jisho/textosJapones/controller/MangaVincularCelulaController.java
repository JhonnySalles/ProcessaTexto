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

public class MangaVincularCelulaController implements Initializable {

	@FXML
	public HBox hbRoot;
	
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
	
	public void setDados(VinculoPagina vinculo) {
		originalImagem.setImage(vinculo.getImagemOriginal());
		originalNumero.setText(vinculo.getOriginalPagina().toString());
		originalNomePagina.setText(vinculo.getOriginalNomePagina());
		
		esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
		esquerdaNumero.setText(vinculo.getVinculadoEsquerdaPagina().toString());
		esquerdaNomePagina.setText(vinculo.getVinculadoEsquerdaNomePagina());

		direitaRoot.setVisible(vinculo.getImagemVinculadoDireita() != null);
		direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
		direitaNumero.setText(vinculo.getVinculadoDireitaPagina().toString());
		direitaNomePagina.setText(vinculo.getVinculadoDireitaNomePagina());
	}

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
		
		direitaRoot.managedProperty().bind(direitaRoot.visibleProperty());

	}

	public static URL getFxmlLocate() {
		return MangaVincularCelulaController.class.getResource("/view/MangaVincularCelula.fxml");
	}
}
