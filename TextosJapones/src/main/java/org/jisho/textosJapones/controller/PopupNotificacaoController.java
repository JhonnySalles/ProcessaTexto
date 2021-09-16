package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class PopupNotificacaoController implements Initializable {

	@FXML
	private AnchorPane notificacaoBackground;

	@FXML
	private ImageView imgImagem;

	@FXML
	private Label lblTitulo;

	@FXML
	private Label lblTexto;

	public Double wheight;

	public Image getImagem() {
		return imgImagem.getImage();
	}

	public PopupNotificacaoController setImagem(ImageView imagem) {
		this.imgImagem = imagem;
		return this;
	}

	public String getTitulo() {
		return lblTitulo.getText();
	}

	public PopupNotificacaoController setTitulo(String titulo) {
		this.lblTitulo.setText(titulo);
		return this;
	}

	public String getTexto() {
		return lblTexto.getText();
	}

	public PopupNotificacaoController setTexto(String texto) {
		lblTexto.setText(texto);

		if (texto.length() <= 80) {
			notificacaoBackground.setPrefSize(500, 47);
			notificacaoBackground.setMaxSize(500, 47);
			imgImagem.setFitHeight(35);
			imgImagem.setFitWidth(35);
			wheight = 60.0;
		} else {
			if (texto.length() <= 225) {
				notificacaoBackground.setPrefSize(500, 80);
				notificacaoBackground.setMaxSize(500, 80);
				imgImagem.setFitHeight(35);
				imgImagem.setFitWidth(35);
				wheight = 80.0;
			} else {
				notificacaoBackground.setPrefSize(650, 100);
				notificacaoBackground.setMaxSize(650, 100);
				imgImagem.setFitHeight(45);
				imgImagem.setFitWidth(45);
				wheight = 100.0;
			}
		}
		return this;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	public static URL getFxmlLocate() {
		return PopupNotificacaoController.class
				.getResource("/view/PopupNotificacao.fxml");
	}
}
