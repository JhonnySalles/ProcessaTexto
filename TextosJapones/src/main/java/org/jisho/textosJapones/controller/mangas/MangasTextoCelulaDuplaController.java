package org.jisho.textosJapones.controller.mangas;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.components.ImageViewZoom;
import org.jisho.textosJapones.model.entities.VinculoPagina;

import com.jfoenix.controls.JFXSlider;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

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
	public Text esquerdaPagina;

	@FXML
	public AnchorPane direitaRoot;

	@FXML
	public ImageView direitaImagem;

	@FXML
	public JFXSlider direitaSlider;

	@FXML
	public Text direitaPagina;

	public void setDados(VinculoPagina vinculo) {
		if (vinculo == null) {
			esquerdaImagem.setImage(null);
			direitaImagem.setImage(null);
			direitaRoot.setVisible(false);
			esquerdaPagina.setText("");
			direitaPagina.setText("");
		} else {
			esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
			direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
			if (vinculo.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
				direitaRoot.setVisible(true);
			else
				direitaRoot.setVisible(false);

			if (vinculo.getMangaPaginaEsquerda() != null)
				esquerdaPagina.setText("Pag: " + vinculo.getMangaPaginaEsquerda().getNumero() + " - "
						+ vinculo.getMangaPaginaEsquerda().getNomePagina());
			else
				esquerdaPagina.setText("");

			if (vinculo.getMangaPaginaDireita() != null)
				direitaPagina.setText("Pag: " + vinculo.getMangaPaginaDireita().getNumero() + " - "
						+ vinculo.getMangaPaginaDireita().getNomePagina());
			else
				direitaPagina.setText("");

			if (vinculo.getImagemVinculadoEsquerda() != null)
				ImageViewZoom.configura(vinculo.getImagemVinculadoEsquerda(), esquerdaImagem, esquerdaSlider);
			else
				ImageViewZoom.limpa(esquerdaImagem, esquerdaSlider);

			if (vinculo.getImagemVinculadoDireita() != null)
				ImageViewZoom.configura(vinculo.getImagemVinculadoDireita(), direitaImagem, direitaSlider);
			else
				ImageViewZoom.limpa(direitaImagem, direitaSlider);
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		esquerdaImagem.fitWidthProperty().bind(esquerdaRoot.widthProperty());
		esquerdaImagem.fitHeightProperty().bind(esquerdaRoot.heightProperty());
		esquerdaImagem.setPreserveRatio(true);

		direitaImagem.fitWidthProperty().bind(direitaRoot.widthProperty());
		direitaImagem.fitHeightProperty().bind(direitaRoot.heightProperty());
		direitaImagem.setPreserveRatio(true);

		direitaRoot.managedProperty().bind(direitaRoot.visibleProperty());
	}

	public static URL getFxmlLocate() {
		return MangasTextoCelulaDuplaController.class.getResource("/view/mangas/MangaTextoCelulaDupla.fxml");
	}
}
