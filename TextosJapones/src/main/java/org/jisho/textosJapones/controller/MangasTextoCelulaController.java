package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.VinculoPagina;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextArea;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class MangasTextoCelulaController implements Initializable {

	@FXML
	public HBox hbRoot;

	@FXML
	public AnchorPane originalRoot;

	@FXML
	public ImageView originalImagem;

	@FXML
	public JFXSlider originalSlider;

	@FXML
	public JFXTextArea originalTextos;

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

	@FXML
	public JFXTextArea vinculadoTextos;

	public void setDados(VinculoPagina vinculo) {

		String textos = "";

		if (vinculo.getMangaPaginaOriginal() != null)
			vinculo.getMangaPaginaOriginal().getTextos().stream().map(MangaTexto::getTexto)
					.collect(Collectors.joining("\n"));
		originalTextos.setText(textos);
		originalImagem.setImage(vinculo.getImagemOriginal());

		textos = "";
		if (vinculo.getMangaPaginaEsquerda() != null)
			textos = vinculo.getMangaPaginaEsquerda().getTextos().stream().map(MangaTexto::getTexto)
					.collect(Collectors.joining("\n"));
		vinculadoTextos.setText(textos);
		esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());

		direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
		if (vinculo.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA) {
			textos = "";
			if (vinculo.getMangaPaginaDireita() != null)
				textos = vinculo.getMangaPaginaDireita().getTextos().stream().map(MangaTexto::getTexto)
						.collect(Collectors.joining("\n"));
			direitaRoot.setVisible(true);
			vinculadoTextos.setText(textos);
		} else {
			direitaRoot.setVisible(false);
			vinculadoTextos.setText("");
			direitaImagem.setImage(null);
		}

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

		originalImagem.fitWidthProperty().bind(originalRoot.widthProperty());
		originalImagem.setPreserveRatio(true);

		esquerdaImagem.fitWidthProperty().bind(originalRoot.widthProperty());
		esquerdaImagem.setPreserveRatio(true);

		direitaImagem.fitWidthProperty().bind(originalRoot.widthProperty());
		direitaImagem.setPreserveRatio(true);

		direitaRoot.managedProperty().bind(direitaRoot.visibleProperty());

		configuraZoom(originalImagem, originalSlider);
		configuraZoom(esquerdaImagem, esquerdaSlider);
		configuraZoom(direitaImagem, direitaSlider);

	}

	public static URL getFxmlLocate() {
		return MangasTextoCelulaController.class.getResource("/view/MangaTextoCelula.fxml");
	}

}
