package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.VinculoPagina;

import com.jfoenix.controls.JFXSlider;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class MangasTextoCelulaDuplaController implements Initializable {

	@FXML
	public HBox hbRoot;

	@FXML
	public AnchorPane esquerdaRoot;

	@FXML
	public ImageView esquerdaImagem;

	@FXML
	public JFXSlider esquerdaSlider;

	@FXML
	public AnchorPane direitaRoot;

	@FXML
	public ImageView direitaImagem;

	@FXML
	public JFXSlider direitaSlider;

	public void setDados(VinculoPagina vinculo) {
		esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
		direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
		if (vinculo.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
			direitaRoot.setVisible(true);
		else
			direitaRoot.setVisible(false);
	}

	private void configuraZoom(ImageView imagem, JFXSlider slider) {
		slider.setMin(1);
		slider.setMax(4);
		slider.setMaxWidth(200);
		slider.setMaxHeight(200);

		slider.valueProperty().addListener(e -> {
			double zoom = slider.getValue();
			imagem.setFitWidth(zoom * 4);
			imagem.setFitHeight(zoom * 3);
		});

		/*
		 * imagem.setOnMousePressed(e->{ initx = e.getSceneX(); inity = e.getSceneY();
		 * });
		 * 
		 * imagem.setOnMouseDragged(e->{ initx = e.getSceneX(); inity = e.getSceneY();
		 * });
		 */
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		esquerdaImagem.fitWidthProperty().bind(esquerdaRoot.widthProperty());
		esquerdaImagem.setPreserveRatio(true);

		direitaImagem.fitWidthProperty().bind(direitaRoot.widthProperty());
		direitaImagem.setPreserveRatio(true);

		direitaRoot.managedProperty().bind(direitaRoot.visibleProperty());

		configuraZoom(esquerdaImagem, esquerdaSlider);
		configuraZoom(direitaImagem, direitaSlider);

	}

	public static URL getFxmlLocate() {
		return MangasTextoCelulaDuplaController.class.getResource("/view/MangaTextoCelulaDupla.fxml");
	}

}
