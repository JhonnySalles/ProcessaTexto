package org.jisho.textosJapones.controller.mangas;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXSlider;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class MangasTextoCelulaController implements Initializable {

	@FXML
	public AnchorPane root;

	@FXML
	public ImageView imagem;

	@FXML
	public JFXSlider slider;

	@FXML
	public Text pagina;

	public void setDados(Image imagemOriginal, String namePagina) {
		imagem.setImage(imagemOriginal);
		pagina.setText(namePagina);
	}

	private double zoom = 0;

	private void configuraZoom() {
		slider.setMin(1);
		slider.setMax(4);
		slider.setValue(1);

		slider.valueProperty().addListener(e -> {
			zoom = slider.getValue();
			imagem.setScaleX(zoom);
			imagem.setScaleY(zoom);
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imagem.fitWidthProperty().bind(root.widthProperty());
		imagem.setPreserveRatio(true);

		configuraZoom();
	}

	public static URL getFxmlLocate() {
		return MangasTextoCelulaController.class.getResource("/view/mangas/MangaTextoCelula.fxml");
	}

}
