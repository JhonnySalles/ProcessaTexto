package org.jisho.textosJapones.controller.mangas;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jisho.textosJapones.model.entities.VinculoPagina;

import java.net.URL;
import java.util.ResourceBundle;

public class MangasVincularCelulaSimplesController implements Initializable {

	final PseudoClass ON_DRAG_SELECIONADO = PseudoClass.getPseudoClass("drag-selecionado");

	@FXML
	public AnchorPane root;

	@FXML
	public ImageView imagem;

	@FXML
	public Text numero;

	@FXML
	public VBox containerNome;

	@FXML
	public Text nomePagina;

	public void setDados(VinculoPagina vinculo) {
		if (vinculo != null) {
			imagem.setImage(vinculo.getImagemOriginal());
			numero.setText(vinculo.getOriginalPagina().toString());
			nomePagina.setText(vinculo.getOriginalNomePagina());
		} else {
			imagem.setImage(null);
			numero.setText("");
			nomePagina.setText("");
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		imagem.fitWidthProperty().bind(root.widthProperty());
		imagem.fitHeightProperty().bind(root.heightProperty());
		imagem.setPreserveRatio(true);
		imagem.fitWidthProperty().bind(root.widthProperty());

	}

	public static URL getFxmlLocate() {
		return MangasVincularCelulaSimplesController.class.getResource("/view/mangas/MangaVincularCelulaSimples.fxml");
	}
}
