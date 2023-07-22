package org.jisho.textosJapones.controller.mangas;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jisho.textosJapones.components.listener.VinculoListener;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.util.Util;

import java.net.URL;
import java.util.ResourceBundle;

public class MangasVincularCelulaPequenaController implements Initializable {

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

	private VinculoListener listener;

	public void setDados(VinculoPagina vinculo) {
		imagem.setImage(vinculo.getImagemVinculadoEsquerda());
		numero.setText(vinculo.getVinculadoEsquerdaPagina().toString());
		nomePagina.setText(vinculo.getVinculadoEsquerdaNomePagina());

		root.setOnDragDetected(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				listener.onDragStart();
				Dragboard db = root.startDragAndDrop(TransferMode.ANY);

				db.setDragView(Util.criaSnapshot(root), event.getX(), event.getY());

				vinculo.onDragOrigem = Pagina.NAO_VINCULADO;
				ClipboardContent content = new ClipboardContent();
				content.put(Util.VINCULO_ITEM_FORMAT, vinculo);
				content.put(Util.NUMERO_PAGINA_ITEM_FORMAT, vinculo.getVinculadoEsquerdaPagina().toString());
				content.putString(vinculo.getVinculadoEsquerdaNomePagina());
				db.setContent(content);

				event.consume();
			}
		});
	}

	public void setListener(VinculoListener listener) {
		this.listener = listener;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		imagem.fitWidthProperty().bind(root.widthProperty());
		imagem.fitHeightProperty().bind(root.heightProperty());
		imagem.setPreserveRatio(true);
		imagem.fitWidthProperty().bind(containerNome.widthProperty());

	}

	public static URL getFxmlLocate() {
		return MangasVincularCelulaPequenaController.class.getResource("/view/mangas/MangaVincularCelulaPequena.fxml");
	}
}
