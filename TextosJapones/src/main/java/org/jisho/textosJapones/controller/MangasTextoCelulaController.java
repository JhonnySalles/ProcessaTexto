package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXSlider;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class MangasTextoCelulaController implements Initializable {

	@FXML
	public AnchorPane root;

	@FXML
	public ImageView imagem;

	@FXML
	public JFXSlider slider;

	public void setDados(Image imagemOriginal) {
		imagem.setImage(imagemOriginal);
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
		imagem.fitWidthProperty().bind(root.widthProperty());
		imagem.setPreserveRatio(true);

		configuraZoom(imagem, slider);
	}

	public static URL getFxmlLocate() {
		return MangasTextoCelulaController.class.getResource("/view/MangaTextoCelula.fxml");
	}

}
