package org.jisho.textosJapones.controller.mangas;

import com.jfoenix.controls.JFXSlider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.jisho.textosJapones.components.ImageViewZoom;

import java.net.URL;
import java.util.ResourceBundle;

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
		
		ImageViewZoom.configura(imagemOriginal, imagem, slider);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imagem.fitWidthProperty().bind(root.widthProperty());
		imagem.fitHeightProperty().bind(root.heightProperty());
		imagem.setPreserveRatio(true);
	}

	public static URL getFxmlLocate() {
		return MangasTextoCelulaController.class.getResource("/view/mangas/MangaTextoCelula.fxml");
	}

}
