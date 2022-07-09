package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.listener.VinculoListener;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MangaVincularCelulaController implements Initializable {

	final PseudoClass ON_DRAG_SELECIONADO = PseudoClass.getPseudoClass("drag-selecionado");

	@FXML
	public HBox hbRoot;

	@FXML
	public AnchorPane originalRoot;

	@FXML
	public ImageView originalImagem;

	@FXML
	public Text originalNumero;
	
	@FXML
	public VBox originalNomeRoot;

	@FXML
	public Text originalNomePagina;

	@FXML
	public AnchorPane esquerdaRoot;

	@FXML
	public ImageView esquerdaImagem;

	@FXML
	public Text esquerdaNumero;
	
	@FXML
	public VBox esquerdaNomeRoot;

	@FXML
	public Text esquerdaNomePagina;

	@FXML
	public AnchorPane direitaRoot;

	@FXML
	public ImageView direitaImagem;

	@FXML
	public Text direitaNumero;

	@FXML
	public VBox direitaNomeRoot;
	
	@FXML
	public Text direitaNomePagina;

	private VinculoListener listener;

	public void setDados(VinculoPagina vinculo) {
		originalImagem.setImage(vinculo.getImagemOriginal());
		originalNumero.setText(vinculo.getOriginalPagina().toString());
		originalNomePagina.setText(vinculo.getOriginalNomePagina());
		
		hbRoot.setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (vinculo.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
					direitaRoot.setVisible(true);
					direitaRoot.setPrefWidth(esquerdaRoot.getWidth());
				}
	
			}
		});
		
		hbRoot.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (!vinculo.isImagemDupla && vinculo.getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA)
					direitaRoot.setVisible(false);
	
			}
		});
		
		hbRoot.setOnDragDone(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (!vinculo.isImagemDupla && vinculo.getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA)
					direitaRoot.setVisible(false);
			}
		});


		if (vinculo.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
			limpaDrop(direitaRoot);
			esquerdaRoot.setOnDragDetected(null);
			esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
			esquerdaNumero.setText("");
			esquerdaNomePagina.setText("");
			esquerdaNomeRoot.setVisible(false);
		} else {
			configuraDrop(direitaRoot, vinculo, Pagina.VINCULADO_DIREITA);

			esquerdaRoot.setOnDragDetected(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					listener.onDragStart();
					Dragboard db = esquerdaRoot.startDragAndDrop(TransferMode.ANY);

					db.setDragView(Util.criaSnapshot(esquerdaRoot), event.getX(), event.getY());

					vinculo.onDragOrigem = Pagina.VINCULADO_ESQUERDA;
					ClipboardContent content = new ClipboardContent();
					content.put(Util.VINCULO_ITEM_FORMAT, vinculo);
					content.put(Util.NUMERO_PAGINA_ITEM_FORMAT, vinculo.getVinculadoEsquerdaPagina().toString());
					content.putString(vinculo.getVinculadoEsquerdaNomePagina());
					db.setContent(content);

					event.consume();
				}
			});

			esquerdaRoot.setOnDragDone(new EventHandler<DragEvent>() {
				public void handle(DragEvent event) {
					listener.onDragEnd();
					direitaRoot.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);
				}
			});

			esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
			esquerdaNumero.setText(vinculo.getVinculadoEsquerdaPagina().toString());
			esquerdaNomePagina.setText(vinculo.getVinculadoEsquerdaNomePagina());
			esquerdaNomeRoot.setVisible(true);
		}

		configuraDrop(esquerdaRoot, vinculo, Pagina.VINCULADO_ESQUERDA);

		if (vinculo.getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA) {
			direitaRoot.setOnDragDetected(null);
			direitaRoot.setVisible(false);
			direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
			direitaNumero.setText("");
			direitaNomePagina.setText("");
		} else {
			direitaRoot.setOnDragDetected(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					listener.onDragStart();

					Dragboard db = esquerdaRoot.startDragAndDrop(TransferMode.ANY);

					vinculo.onDragOrigem = Pagina.VINCULADO_DIREITA;
					ClipboardContent content = new ClipboardContent();
					content.put(Util.VINCULO_ITEM_FORMAT, vinculo);
					content.put(Util.NUMERO_PAGINA_ITEM_FORMAT, vinculo.getVinculadoEsquerdaPagina().toString());
					content.putString(vinculo.getVinculadoEsquerdaNomePagina());
					db.setContent(content);

					event.consume();
				}
			});

			direitaRoot.setOnDragDone(new EventHandler<DragEvent>() {
				public void handle(DragEvent event) {
					listener.onDragEnd();
					direitaRoot.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);
				}
			});

			direitaRoot.setVisible(vinculo.getImagemVinculadoDireita() != null);
			direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
			direitaNumero.setText(vinculo.getVinculadoDireitaPagina().toString());
			direitaNomePagina.setText(vinculo.getVinculadoDireitaNomePagina());
		}

	}

	private void limpaDrop(AnchorPane pane) {
		pane.setOnDragEntered(null);
		pane.setOnDragExited(null);

	}

	private void configuraDrop(AnchorPane pane,  VinculoPagina destino, Pagina tipo) {
		pane.setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.getGestureSource() != pane && event.getDragboard().hasContent(Util.VINCULO_ITEM_FORMAT))
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

				event.consume();
			}
		});

		pane.setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.getGestureSource() != pane)
					pane.pseudoClassStateChanged(ON_DRAG_SELECIONADO, true);
		
				event.consume();
			}
		});

		pane.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				pane.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);

				event.consume();
			}
		});

		pane.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasContent(Util.VINCULO_ITEM_FORMAT)) {
					VinculoPagina vinculo = (VinculoPagina) db.getContent(Util.VINCULO_ITEM_FORMAT);

					listener.onDrop(vinculo.onDragOrigem, vinculo, tipo, destino);
					success = true;
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});
	}

	public void setListener(VinculoListener listener) {
		this.listener = listener;
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
